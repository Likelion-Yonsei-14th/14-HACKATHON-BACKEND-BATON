package com.likelion.yonsei.baton.domain.baton.service;

import com.likelion.yonsei.baton.domain.baton.branch.entity.Branch;
import com.likelion.yonsei.baton.domain.baton.branch.repository.BranchRepository;
import com.likelion.yonsei.baton.domain.baton.entity.Baton;
import com.likelion.yonsei.baton.domain.baton.entity.BatonStatus;
import com.likelion.yonsei.baton.domain.baton.repository.BatonRepository;
import com.likelion.yonsei.baton.domain.classification.entity.Classification;
import com.likelion.yonsei.baton.domain.classification.service.ClassificationService;
import com.likelion.yonsei.baton.domain.execution.entity.Execution;
import com.likelion.yonsei.baton.domain.execution.service.ActionExecutor;
import com.likelion.yonsei.baton.domain.message.entity.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Orchestrates the "counterpart reply arrived" pipeline documented in the API spec's 핵심_API_흐름:
 * find the active BATON for the conversation, classify the reply against its approved branches,
 * run the rule engine, and either execute or send the BATON to PENDING_REVIEW.
 */
@Service
public class ReplyProcessingService {

	private static final Logger log = LoggerFactory.getLogger(ReplyProcessingService.class);

	private final BatonRepository batonRepository;
	private final BranchRepository branchRepository;
	private final ClassificationService classificationService;
	private final RuleEngine ruleEngine;
	private final ActionExecutor actionExecutor;
	private final Clock clock;

	public ReplyProcessingService(
			BatonRepository batonRepository,
			BranchRepository branchRepository,
			ClassificationService classificationService,
			RuleEngine ruleEngine,
			ActionExecutor actionExecutor,
			Clock clock
	) {
		this.batonRepository = batonRepository;
		this.branchRepository = branchRepository;
		this.classificationService = classificationService;
		this.ruleEngine = ruleEngine;
		this.actionExecutor = actionExecutor;
		this.clock = clock;
	}

	@Transactional
	public void process(Long conversationId, Message reply) {
		if (reply.isBatonGenerated()) {
			// A BATON-generated message must never re-trigger another BATON (AGENTS.md "One Baton, One Handoff").
			return;
		}

		Optional<Baton> activeBaton = batonRepository.findByConversationIdAndStatusIn(
				conversationId, List.of(BatonStatus.WAITING));
		if (activeBaton.isEmpty()) {
			return;
		}

		Baton baton = activeBaton.get();
		baton.receiveReply(reply.getId());

		List<Branch> branches = branchRepository.findByBatonIdOrderBySortOrderAsc(baton.getId());
		Classification classification = classificationService.classify(baton, reply, branches);

		Branch selectedBranch = classification.getSelectedBranchId() == null
				? null
				: branches.stream()
						.filter(b -> b.getId().equals(classification.getSelectedBranchId()))
						.findFirst()
						.orElse(null);

		RuleEngine.Verdict verdict = ruleEngine.evaluate(classification, selectedBranch);
		log.info("BATON {} classified reply {} -> {} ({})", baton.getId(), reply.getId(), verdict.decision(), verdict.reason());

		if (!verdict.isExecute()) {
			if (classification.getResultStatus().name().equals("MATCHED")) {
				classification.markGuardrailRejected();
			}
			baton.requireReview();
			return;
		}

		Execution execution = actionExecutor.execute(baton, selectedBranch.getId(), classification.getId(), selectedBranch);
		LocalDateTime now = LocalDateTime.now(clock);
		baton.markExecuted(now);
		baton.complete(now);
		if (execution.getExecutionStatus().name().equals("FAILED")) {
			baton.markError();
		}
	}
}
