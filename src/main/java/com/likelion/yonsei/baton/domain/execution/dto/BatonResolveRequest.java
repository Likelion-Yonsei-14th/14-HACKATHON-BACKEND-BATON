package com.likelion.yonsei.baton.domain.execution.dto;

import jakarta.validation.constraints.NotNull;

public record BatonResolveRequest(
		@NotNull ResolutionType resolutionType,
		Long branchId,
		String manualResponse
) {

	public enum ResolutionType {
		SELECT_BRANCH,
		MANUAL_REPLY,
		CANCEL
	}
}
