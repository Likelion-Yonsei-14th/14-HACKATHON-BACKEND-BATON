package com.likelion.yonsei.baton.domain.execution.dto;

import com.likelion.yonsei.baton.domain.baton.branch.entity.ActionType;
import com.likelion.yonsei.baton.domain.execution.entity.Execution;
import com.likelion.yonsei.baton.domain.execution.entity.ExecutionStatus;

import java.time.LocalDateTime;

public record ExecutionSummaryResponse(
		Long id,
		Long classificationId,
		Long resultMessageId,
		ActionType actionType,
		ExecutionStatus executionStatus,
		LocalDateTime executedAt,
		String failureReason
) {

	public static ExecutionSummaryResponse from(Execution execution) {
		return new ExecutionSummaryResponse(
				execution.getId(),
				execution.getClassificationId(),
				execution.getResultMessageId(),
				execution.getActionType(),
				execution.getExecutionStatus(),
				execution.getExecutedAt(),
				execution.getFailureReason()
		);
	}
}
