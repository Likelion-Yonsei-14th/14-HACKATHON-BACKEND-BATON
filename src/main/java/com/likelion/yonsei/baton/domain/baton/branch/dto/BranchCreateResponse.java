package com.likelion.yonsei.baton.domain.baton.branch.dto;

import com.likelion.yonsei.baton.domain.baton.branch.entity.ActionType;
import com.likelion.yonsei.baton.domain.baton.branch.entity.Branch;

public record BranchCreateResponse(
		Long id,
		Long batonId,
		String name,
		String conditionText,
		String decisionText,
		ActionType actionType
) {

	public static BranchCreateResponse from(Branch branch) {
		return new BranchCreateResponse(
				branch.getId(), branch.getBatonId(), branch.getName(), branch.getConditionText(), branch.getDecisionText(), branch.getActionType());
	}
}
