package com.likelion.yonsei.baton.domain.baton.branch.service;

import com.likelion.yonsei.baton.common.exception.BusinessException;
import com.likelion.yonsei.baton.domain.baton.branch.dto.AiBranchDraft;
import com.likelion.yonsei.baton.domain.baton.branch.entity.Branch;
import com.likelion.yonsei.baton.domain.baton.branch.repository.BranchRepository;
import com.likelion.yonsei.baton.domain.baton.entity.Baton;
import com.likelion.yonsei.baton.domain.baton.service.BatonService;
import com.likelion.yonsei.baton.domain.message.entity.Message;
import com.likelion.yonsei.baton.domain.message.repository.MessageRepository;
import com.likelion.yonsei.baton.integration.llm.LlmRouter;
import com.likelion.yonsei.baton.integration.openai.exception.OpenAiErrorCode;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Service
public class BranchGenerationService {

	private static final int CONTEXT_MESSAGE_LIMIT = 15;

	private static final String SYSTEM_PROMPT = """
			You are BATON's branch generation assistant. BATON lets a user pre-approve, before going offline, \
			what should happen for each way a counterpart might reply to a message. You read the recent \
			conversation and the trigger message, then propose 2-4 mutually distinguishable Condition -> Decision \
			-> Action branches covering the plausible replies.

			Rules:
			- Each branch must have a clear, checkable condition_text (what the reply looks like).
			- decision_text is the decision the user is pre-approving for that condition.
			- response_text is the actual pre-approved reply to send (in the conversation's language), or null if \
			  the action does not send a reply.
			- action_type must be one of: SEND_REPLY, REQUEST_HUMAN, FORWARD, NOTIFY.
			- execution_mode must be one of: AUTO, MANUAL. Use MANUAL when the branch is uncertain or high-stakes.
			- Never invent commitments, dates, or costs beyond what the conversation supports.
			- Always include one catch-all branch (e.g. unexpected topic or out-of-range) with action_type \
			  REQUEST_HUMAN and execution_mode MANUAL.

			Respond with ONLY a JSON object of the exact shape:
			{"branches": [{"name": string, "condition_text": string, "decision_text": string, \
			"response_text": string | null, "action_type": string, "execution_mode": string}]}
			""";

	/**
	 * Qwen3 0.6B (the local fallback model) can't reliably follow the full rule set above — it answers
	 * in English, invents field values, and leaves response_text null even for SEND_REPLY branches. A
	 * worked example and a much shorter rule set gets it to actually produce usable Korean branches.
	 */
	private static final String LOCAL_SYSTEM_PROMPT = """
			/no_think
			You generate 2-3 short reply-branches for a chat message. ALWAYS write name, condition_text, \
			decision_text, response_text in the SAME language as the conversation below (Korean if the \
			conversation is Korean). response_text must be an actual ready-to-send reply, never null or empty, \
			for action_type SEND_REPLY.

			Output ONLY this JSON, nothing else:
			{"branches": [{"name": string, "condition_text": string, "decision_text": string, \
			"response_text": string, "action_type": "SEND_REPLY", "execution_mode": "AUTO"}]}

			Example — conversation "내일 3시에 만날까요?":
			{"branches": [{"name": "수락", "condition_text": "상대가 시간에 동의함", \
			"decision_text": "약속을 확정한다", "response_text": "네 좋습니다, 내일 3시에 뵙겠습니다!", \
			"action_type": "SEND_REPLY", "execution_mode": "AUTO"}, \
			{"name": "다른 시간 제안", "condition_text": "상대가 다른 시간을 제안함", \
			"decision_text": "제안된 시간을 확인 후 답한다", "response_text": "알겠습니다, 그 시간으로 조정하겠습니다.", \
			"action_type": "SEND_REPLY", "execution_mode": "AUTO"}]}
			""";

	private final LlmRouter llmRouter;
	private final ObjectMapper objectMapper;
	private final BatonService batonService;
	private final MessageRepository messageRepository;
	private final BranchRepository branchRepository;

	public BranchGenerationService(
			LlmRouter llmRouter,
			ObjectMapper objectMapper,
			BatonService batonService,
			MessageRepository messageRepository,
			BranchRepository branchRepository
	) {
		this.llmRouter = llmRouter;
		this.objectMapper = objectMapper;
		this.batonService = batonService;
		this.messageRepository = messageRepository;
		this.branchRepository = branchRepository;
	}

	@Transactional
	public List<Branch> generate(Long batonId, Long userId, String additionalInstruction) {
		Baton baton = batonService.getById(batonId, userId);

		List<Message> recentMessages = messageRepository.findByConversationIdOrderBySentAtDesc(
				baton.getConversationId(), PageRequest.of(0, CONTEXT_MESSAGE_LIMIT));
		String userPrompt = buildUserPrompt(recentMessages, additionalInstruction);

		String systemPrompt = llmRouter.isLocal(userId) ? LOCAL_SYSTEM_PROMPT : SYSTEM_PROMPT;
		String json = llmRouter.forUser(userId).chatJson(systemPrompt, userPrompt);
		AiBranchDraft.Envelope envelope = parse(json);

		int sortOrder = 0;
		List<Branch> branches = new java.util.ArrayList<>();
		for (AiBranchDraft draft : envelope.branches()) {
			Branch branch = new Branch(
					baton.getId(),
					draft.name(),
					null,
					draft.conditionText(),
					null,
					draft.decisionText(),
					draft.responseText(),
					draft.actionType(),
					null,
					draft.executionMode(),
					sortOrder++
			);
			branches.add(branchRepository.save(branch));
		}
		return branches;
	}

	private String buildUserPrompt(List<Message> recentMessagesNewestFirst, String additionalInstruction) {
		StringBuilder sb = new StringBuilder("Recent conversation (oldest to newest):\n");
		for (int i = recentMessagesNewestFirst.size() - 1; i >= 0; i--) {
			Message message = recentMessagesNewestFirst.get(i);
			sb.append("- [").append(message.getSenderType()).append("] ").append(message.getContent()).append('\n');
		}
		if (additionalInstruction != null && !additionalInstruction.isBlank()) {
			sb.append("\nAdditional instruction from the user: ").append(additionalInstruction).append('\n');
		}
		return sb.toString();
	}

	private AiBranchDraft.Envelope parse(String json) {
		try {
			return objectMapper.readValue(json, AiBranchDraft.Envelope.class);
		} catch (Exception e) {
			throw new BusinessException(OpenAiErrorCode.INVALID_REQUEST, "OpenAI 응답을 Branch 형식으로 해석하지 못했습니다.");
		}
	}
}
