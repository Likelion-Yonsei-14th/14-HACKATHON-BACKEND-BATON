package com.likelion.yonsei.baton.domain.platform.dto;

import com.likelion.yonsei.baton.domain.platform.entity.ConnectionStatus;
import com.likelion.yonsei.baton.domain.platform.entity.PlatformConnection;
import com.likelion.yonsei.baton.domain.platform.entity.PlatformType;

import java.time.LocalDateTime;

public record PlatformConnectionResponse(
		Long id,
		PlatformType platformType,
		String workspaceId,
		String workspaceName,
		ConnectionStatus connectionStatus,
		LocalDateTime tokenExpiresAt,
		LocalDateTime lastSyncedAt
) {

	public static PlatformConnectionResponse from(PlatformConnection connection) {
		return new PlatformConnectionResponse(
				connection.getId(),
				connection.getPlatformType(),
				connection.getWorkspaceId(),
				connection.getWorkspaceName(),
				connection.getConnectionStatus(),
				connection.getTokenExpiresAt(),
				connection.getLastSyncedAt()
		);
	}
}
