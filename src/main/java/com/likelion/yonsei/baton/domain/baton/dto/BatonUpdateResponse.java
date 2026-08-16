package com.likelion.yonsei.baton.domain.baton.dto;

import com.likelion.yonsei.baton.domain.baton.entity.BatonStatus;

import java.time.LocalDateTime;

public record BatonUpdateResponse(
		Long id,
		BatonStatus status,
		boolean autoSendEnabled,
		LocalDateTime expiresAt,
		LocalDateTime updatedAt
) {
}
