package com.likelion.yonsei.baton.domain.baton.branch.dto;

import com.likelion.yonsei.baton.domain.baton.branch.entity.ActionType;
import com.likelion.yonsei.baton.domain.baton.branch.entity.Branch;
import com.likelion.yonsei.baton.domain.baton.branch.entity.ExecutionMode;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

public record BranchResponse(
		Long id,
		Long batonId,
		String name,
		String description,
		String conditionText,
		JsonNode conditionRuleJson,
		String decisionText,
		String responseText,
		ActionType actionType,
		JsonNode actionConfigJson,
		ExecutionMode executionMode,
		int sortOrder
) {

	public static BranchResponse from(Branch branch, ObjectMapper objectMapper) {
		return new BranchResponse(
				branch.getId(),
				branch.getBatonId(),
				branch.getName(),
				branch.getDescription(),
				branch.getConditionText(),
				readTree(objectMapper, branch.getConditionRuleJson()),
				branch.getDecisionText(),
				branch.getResponseText(),
				branch.getActionType(),
				readTree(objectMapper, branch.getActionConfigJson()),
				branch.getExecutionMode(),
				branch.getSortOrder()
		);
	}

	private static JsonNode readTree(ObjectMapper objectMapper, String json) {
		if (json == null) {
			return null;
		}
		try {
			return objectMapper.readTree(json);
		} catch (Exception e) {
			return null;
		}
	}
}
