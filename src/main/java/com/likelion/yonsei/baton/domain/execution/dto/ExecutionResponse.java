package com.likelion.yonsei.baton.domain.execution.dto;

import com.likelion.yonsei.baton.domain.baton.branch.entity.ActionType;
import com.likelion.yonsei.baton.domain.execution.entity.Execution;
import com.likelion.yonsei.baton.domain.execution.entity.ExecutionStatus;

import java.time.LocalDateTime;

public record ExecutionResponse(
		Long id,
		Long batonId,
		Long classificationId,
		Long resultMessageId,
		ActionType actionType,
		ExecutionStatus executionStatus,
		LocalDateTime executedAt,
		String failureReason,
		LocalDateTime createdAt
) {

	public static ExecutionResponse from(Execution execution) {
		return new ExecutionResponse(
				execution.getId(),
				execution.getBatonId(),
				execution.getClassificationId(),
				execution.getResultMessageId(),
				execution.getActionType(),
				execution.getExecutionStatus(),
				execution.getExecutedAt(),
				execution.getFailureReason(),
				execution.getCreatedAt()
		);
	}
}
