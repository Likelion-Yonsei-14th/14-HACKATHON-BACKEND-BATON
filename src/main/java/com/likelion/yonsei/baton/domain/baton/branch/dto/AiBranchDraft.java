package com.likelion.yonsei.baton.domain.baton.branch.dto;

import com.likelion.yonsei.baton.domain.baton.branch.entity.ActionType;
import com.likelion.yonsei.baton.domain.baton.branch.entity.ExecutionMode;

import java.util.List;

/** Parsed shape of the OpenAI structured-output response for AI branch generation. */
public record AiBranchDraft(
		String name,
		String conditionText,
		String decisionText,
		String responseText,
		ActionType actionType,
		ExecutionMode executionMode
) {

	public record Envelope(List<AiBranchDraft> branches) {
	}
}
