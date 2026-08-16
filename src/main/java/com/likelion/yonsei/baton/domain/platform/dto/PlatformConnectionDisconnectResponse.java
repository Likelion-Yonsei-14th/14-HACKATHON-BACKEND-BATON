package com.likelion.yonsei.baton.domain.platform.dto;

import com.likelion.yonsei.baton.domain.platform.entity.ConnectionStatus;

import java.time.LocalDateTime;

public record PlatformConnectionDisconnectResponse(
		Long id,
		ConnectionStatus connectionStatus,
		LocalDateTime updatedAt
) {
}
