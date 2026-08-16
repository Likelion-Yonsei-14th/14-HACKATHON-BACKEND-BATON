package com.likelion.yonsei.baton.domain.baton.dto;

import java.time.LocalDateTime;

public record BatonUpdateRequest(
		Boolean autoSendEnabled,
		LocalDateTime expiresAt
) {
}
