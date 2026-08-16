package com.likelion.yonsei.baton.domain.baton.branch.dto;

import com.likelion.yonsei.baton.domain.baton.branch.entity.ActionType;
import com.likelion.yonsei.baton.domain.baton.branch.entity.ExecutionMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BranchCreateRequest(
		@NotBlank String name,
		String description,
		@NotBlank String conditionText,
		Object conditionRuleJson,
		@NotBlank String decisionText,
		String responseText,
		@NotNull ActionType actionType,
		Object actionConfigJson,
		@NotNull ExecutionMode executionMode,
		@NotNull Integer sortOrder
) {
}
