package com.likelion.yonsei.baton.domain.baton.dto;

import com.likelion.yonsei.baton.domain.baton.entity.Baton;
import com.likelion.yonsei.baton.domain.baton.entity.BatonStatus;

import java.time.LocalDateTime;

public record BatonResponse(
		Long id,
		Long conversationId,
		Long triggerMessageId,
		Long replyMessageId,
		BatonStatus status,
		boolean autoSendEnabled,
		LocalDateTime expiresAt,
		LocalDateTime activatedAt,
		LocalDateTime completedAt
) {

	public static BatonResponse from(Baton baton) {
		return new BatonResponse(
				baton.getId(),
				baton.getConversationId(),
				baton.getTriggerMessageId(),
				baton.getReplyMessageId(),
				baton.getStatus(),
				baton.isAutoSendEnabled(),
				baton.getExpiresAt(),
				baton.getActivatedAt(),
				baton.getCompletedAt()
		);
	}
}
