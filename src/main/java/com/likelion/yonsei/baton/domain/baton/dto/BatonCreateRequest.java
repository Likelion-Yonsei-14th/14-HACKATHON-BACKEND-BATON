package com.likelion.yonsei.baton.domain.baton.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record BatonCreateRequest(
		@NotNull Long conversationId,
		@NotNull Long triggerMessageId,
		@NotNull Boolean autoSendEnabled,
		LocalDateTime expiresAt
) {
}
