package com.likelion.yonsei.baton.domain.baton.dto;

import com.likelion.yonsei.baton.domain.baton.entity.Baton;
import com.likelion.yonsei.baton.domain.baton.entity.BatonStatus;

import java.time.LocalDateTime;

public record BatonCreateResponse(
		Long id,
		Long conversationId,
		Long triggerMessageId,
		Long replyMessageId,
		BatonStatus status,
		boolean autoSendEnabled,
		LocalDateTime expiresAt,
		LocalDateTime createdAt
) {

	public static BatonCreateResponse from(Baton baton) {
		return new BatonCreateResponse(
				baton.getId(),
				baton.getConversationId(),
				baton.getTriggerMessageId(),
				baton.getReplyMessageId(),
				baton.getStatus(),
				baton.isAutoSendEnabled(),
				baton.getExpiresAt(),
				baton.getCreatedAt()
		);
	}
}
