package com.likelion.yonsei.baton.domain.baton.dto;

import com.likelion.yonsei.baton.domain.baton.entity.Baton;
import com.likelion.yonsei.baton.domain.baton.entity.BatonStatus;

import java.time.LocalDateTime;

public record BatonSummaryResponse(
		Long id,
		Long conversationId,
		BatonStatus status,
		boolean autoSendEnabled,
		LocalDateTime expiresAt
) {

	public static BatonSummaryResponse from(Baton baton) {
		return new BatonSummaryResponse(
				baton.getId(),
				baton.getConversationId(),
				baton.getStatus(),
				baton.isAutoSendEnabled(),
				baton.getExpiresAt()
		);
	}
}
