package com.likelion.yonsei.baton.domain.platform.dto;

import com.likelion.yonsei.baton.domain.platform.entity.ConnectionStatus;
import com.likelion.yonsei.baton.domain.platform.entity.PlatformConnection;
import com.likelion.yonsei.baton.domain.platform.entity.PlatformType;

import java.time.LocalDateTime;

public record PlatformConnectionSummaryResponse(
		Long id,
		PlatformType platformType,
		String workspaceId,
		String workspaceName,
		ConnectionStatus connectionStatus,
		LocalDateTime lastSyncedAt
) {

	public static PlatformConnectionSummaryResponse from(PlatformConnection connection) {
		return new PlatformConnectionSummaryResponse(
				connection.getId(),
				connection.getPlatformType(),
				connection.getWorkspaceId(),
				connection.getWorkspaceName(),
				connection.getConnectionStatus(),
				connection.getLastSyncedAt()
		);
	}
}
