package com.likelion.yonsei.baton.domain.execution.dto;

import com.likelion.yonsei.baton.domain.baton.entity.BatonStatus;

public record BatonResolveResponse(
		Long batonId,
		BatonStatus status,
		Long executionId,
		Long resultMessageId
) {
}
