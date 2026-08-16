package com.likelion.yonsei.baton.domain.message.dto;

import java.time.LocalDateTime;

public record MessageSyncResponse(
		Long conversationId,
		int syncedCount,
		Long lastMessageId,
		LocalDateTime lastSyncedAt
) {
}
