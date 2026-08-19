package com.likelion.yonsei.baton.domain.classification.service;

import com.likelion.yonsei.baton.common.exception.BusinessException;
import com.likelion.yonsei.baton.domain.baton.branch.entity.Branch;
import com.likelion.yonsei.baton.domain.baton.entity.Baton;
import com.likelion.yonsei.baton.domain.baton.service.BatonService;
import com.likelion.yonsei.baton.domain.classification.dto.AiClassificationResult;
import com.likelion.yonsei.baton.domain.classification.entity.Classification;
import com.likelion.yonsei.baton.domain.classification.entity.ClassificationResultStatus;
import com.likelion.yonsei.baton.domain.classification.exception.ClassificationErrorCode;
import com.likelion.yonsei.baton.domain.classification.repository.ClassificationRepository;
import com.likelion.yonsei.baton.domain.message.entity.Message;
import com.likelion.yonsei.baton.integration.llm.LlmRouter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ClassificationService {

	private static final BigDecimal LOW_CONFIDENCE_THRESHOLD = new BigDecimal("0.7");

	private static final AiClassificationResult UNPARSEABLE_RESULT = new AiClassificationResult(
			null, BigDecimal.ZERO, true, false, null, "OpenAI 응답을 해석하지 못해 검토가 필요합니다.");

	private static final String SYSTEM_PROMPT = """
			You are BATON's reply classification assistant. The user pre-approved a fixed set of \
			Condition -> Decision -> Action branches before going offline. You now see the counterpart's \
			actual reply and must decide which single branch it matches, if any.

			Rules:
			- You may only select a branch id from the list given to you. Never invent a new branch.
			- If the reply plausibly matches more than one branch, or matches none, or raises a new question \
			  outside every branch's condition, do not force a match: set ambiguous or contains_new_question \
			  accordingly and selected_branch_id to null.
			- confidence is your calibrated certainty in the match, from 0 to 1.
			- extracted_data holds any concrete facts the reply stated (dates, numbers, choices) as a flat JSON object.
			- reasoning_summary is a short (<= 2 sentences), user-facing explanation. Never include internal \
			  chain-of-thought.

			Respond with ONLY a JSON object of the exact shape:
			{"selected_branch_id": number | null, "confidence": number, "ambiguous": boolean, \
			"contains_new_question": boolean, "extracted_data": object, "reasoning_summary": string}
			""";

	/** Qwen3 0.6B needs a much shorter prompt plus a worked example to reliably produce this shape at all. */
	private static final String LOCAL_SYSTEM_PROMPT = """
			/no_think
			Pick which branch id the reply matches. If the reply itself asks a new question (not just \
			answering), set contains_new_question true and selected_branch_id null, regardless of confidence. \
			Write reasoning_summary in the SAME language as the reply (Korean if Korean).

			Output ONLY this JSON:
			{"selected_branch_id": number or null, "confidence": number, "ambiguous": boolean, \
			"contains_new_question": boolean, "extracted_data": {}, "reasoning_summary": string}

			Example 1 — branches: [{"id":1,"condition":"상대가 동의함"},{"id":2,"condition":"그 외 모든 경우"}], \
			reply: "네 좋아요":
			{"selected_branch_id": 1, "confidence": 0.9, "ambiguous": false, "contains_new_question": false, \
			"extracted_data": {}, "reasoning_summary": "상대가 동의를 표현함"}

			Example 2 — same branches, reply: "네 가능합니다! 몇시가 편하신가요?" (reply asks its own question back):
			{"selected_branch_id": null, "confidence": 0.3, "ambiguous": false, "contains_new_question": true, \
			"extracted_data": {}, "reasoning_summary": "상대가 동의하면서 새로운 질문(시간)을 되물음"}
			""";

	private final ClassificationRepository classificationRepository;
	private final BatonService batonService;
	private final LlmRouter llmRouter;
	private final ObjectMapper objectMapper;

	public ClassificationService(
			ClassificationRepository classificationRepository,
			BatonService batonService,
			LlmRouter llmRouter,
			ObjectMapper objectMapper
	) {
		this.classificationRepository = classificationRepository;
		this.batonService = batonService;
		this.llmRouter = llmRouter;
		this.objectMapper = objectMapper;
	}

	public List<Classification> list(Long batonId, Long userId) {
		batonService.getById(batonId, userId);
		return classificationRepository.findByBatonIdOrderByCreatedAtDesc(batonId);
	}

	public Classification getById(Long id, Long userId) {
		Classification classification = classificationRepository.findById(id)
				.orElseThrow(() -> new BusinessException(ClassificationErrorCode.CLASSIFICATION_NOT_FOUND));
		batonService.getById(classification.getBatonId(), userId);
		return classification;
	}

	public Classification getByIdInternal(Long id) {
		return classificationRepository.findById(id)
				.orElseThrow(() -> new BusinessException(ClassificationErrorCode.CLASSIFICATION_NOT_FOUND));
	}

	/** Called only from the internal reply-processing pipeline (no public POST endpoint exists for Classification). */
	@Transactional
	public Classification classify(Baton baton, Message reply, List<Branch> branches) {
		String userPrompt = buildUserPrompt(reply, branches);
		String systemPrompt = llmRouter.isLocal(baton.getUserId()) ? LOCAL_SYSTEM_PROMPT : SYSTEM_PROMPT;
		String json = llmRouter.forUser(baton.getUserId()).chatJson(systemPrompt, userPrompt);
		AiClassificationResult result = parse(json);

		boolean branchIdValid = result.selectedBranchId() != null
				&& branches.stream().anyMatch(b -> b.getId().equals(result.selectedBranchId()));

		ClassificationResultStatus status = resolveStatus(result, branchIdValid);
		String extractedJson = result.extractedData() != null ? objectMapper.writeValueAsString(result.extractedData()) : null;

		Classification classification = new Classification(
				baton.getId(),
				reply.getId(),
				branchIdValid ? result.selectedBranchId() : null,
				result.confidence(),
				result.ambiguous(),
				result.containsNewQuestion(),
				extractedJson,
				result.reasoningSummary(),
				status,
				llmRouter.modelNameForUser(baton.getUserId())
		);
		return classificationRepository.save(classification);
	}

	private ClassificationResultStatus resolveStatus(AiClassificationResult result, boolean branchIdValid) {
		if (result.ambiguous() || result.containsNewQuestion()) {
			return ClassificationResultStatus.AMBIGUOUS;
		}
		if (!branchIdValid) {
			return ClassificationResultStatus.NO_MATCH;
		}
		if (result.confidence() == null || result.confidence().compareTo(LOW_CONFIDENCE_THRESHOLD) < 0) {
			return ClassificationResultStatus.LOW_CONFIDENCE;
		}
		return ClassificationResultStatus.MATCHED;
	}

	private String buildUserPrompt(Message reply, List<Branch> branches) {
		StringBuilder sb = new StringBuilder("Approved branches:\n");
		for (Branch branch : branches) {
			sb.append("- id=").append(branch.getId())
					.append(", condition=\"").append(branch.getConditionText())
					.append("\", decision=\"").append(branch.getDecisionText()).append("\"\n");
		}
		sb.append("\nCounterpart reply: \"").append(reply.getContent()).append("\"\n");
		return sb.toString();
	}

	private AiClassificationResult parse(String json) {
		try {
			return objectMapper.readValue(json, AiClassificationResult.class);
		} catch (Exception e) {
			// Treat an unparseable structured output as a guardrail rejection, not a crash: never auto-execute on malformed AI output.
			return UNPARSEABLE_RESULT;
		}
	}
}
