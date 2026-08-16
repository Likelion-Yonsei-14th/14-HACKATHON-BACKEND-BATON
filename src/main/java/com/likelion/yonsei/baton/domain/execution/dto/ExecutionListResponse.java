package com.likelion.yonsei.baton.domain.execution.dto;

import java.util.List;

public record ExecutionListResponse(
		List<ExecutionSummaryResponse> executions
) {
}
