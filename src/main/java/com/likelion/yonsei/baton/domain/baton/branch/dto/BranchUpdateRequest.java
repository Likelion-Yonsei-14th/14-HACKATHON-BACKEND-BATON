package com.likelion.yonsei.baton.domain.baton.branch.dto;

import com.likelion.yonsei.baton.domain.baton.branch.entity.ActionType;
import com.likelion.yonsei.baton.domain.baton.branch.entity.ExecutionMode;

public record BranchUpdateRequest(
		String name,
		String description,
		String conditionText,
		Object conditionRuleJson,
		String decisionText,
		String responseText,
		ActionType actionType,
		Object actionConfigJson,
		ExecutionMode executionMode,
		Integer sortOrder
) {
}
