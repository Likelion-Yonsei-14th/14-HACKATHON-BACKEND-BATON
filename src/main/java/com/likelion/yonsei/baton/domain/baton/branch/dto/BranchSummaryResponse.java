package com.likelion.yonsei.baton.domain.baton.branch.dto;

import com.likelion.yonsei.baton.domain.baton.branch.entity.ActionType;
import com.likelion.yonsei.baton.domain.baton.branch.entity.Branch;
import com.likelion.yonsei.baton.domain.baton.branch.entity.ExecutionMode;

public record BranchSummaryResponse(
		Long id,
		String name,
		String conditionText,
		String decisionText,
		String responseText,
		ActionType actionType,
		ExecutionMode executionMode,
		int sortOrder
) {

	public static BranchSummaryResponse from(Branch branch) {
		return new BranchSummaryResponse(
				branch.getId(),
				branch.getName(),
				branch.getConditionText(),
				branch.getDecisionText(),
				branch.getResponseText(),
				branch.getActionType(),
				branch.getExecutionMode(),
				branch.getSortOrder()
		);
	}
}
