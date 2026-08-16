package com.likelion.yonsei.baton.domain.baton.service;

import com.likelion.yonsei.baton.domain.baton.branch.entity.Branch;
import com.likelion.yonsei.baton.domain.baton.branch.entity.ExecutionMode;
import com.likelion.yonsei.baton.domain.classification.entity.Classification;
import com.likelion.yonsei.baton.domain.classification.entity.ClassificationResultStatus;
import org.springframework.stereotype.Component;

/**
 * Deterministic guardrail that decides whether a classified reply may be auto-executed.
 * The LLM interprets; only this rule engine authorizes execution — see AGENTS.md "LLM 연동".
 */
@Component
public class RuleEngine {

	public enum Decision {
		EXECUTE,
		PENDING_REVIEW
	}

	public record Verdict(Decision decision, String reason) {

		public boolean isExecute() {
			return decision == Decision.EXECUTE;
		}
	}

	public Verdict evaluate(Classification classification, Branch selectedBranch) {
		if (classification.getResultStatus() != ClassificationResultStatus.MATCHED) {
			return new Verdict(Decision.PENDING_REVIEW, "classification status is " + classification.getResultStatus());
		}
		if (classification.isAmbiguous() || classification.isContainsNewQuestion()) {
			return new Verdict(Decision.PENDING_REVIEW, "ambiguous or contains a new question");
		}
		if (selectedBranch == null) {
			return new Verdict(Decision.PENDING_REVIEW, "no branch selected");
		}
		if (selectedBranch.getExecutionMode() != ExecutionMode.AUTO) {
			return new Verdict(Decision.PENDING_REVIEW, "matched branch requires manual execution");
		}
		return new Verdict(Decision.EXECUTE, "matched branch approved for automatic execution");
	}
}
