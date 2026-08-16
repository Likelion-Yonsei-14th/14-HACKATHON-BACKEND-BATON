package com.likelion.yonsei.baton.domain.platform.dto;

import java.time.LocalDateTime;

public record ConversationsSyncResponse(
		Long platformConnectionId,
		int createdCount,
		int updatedCount,
		LocalDateTime lastSyncedAt
) {
}
