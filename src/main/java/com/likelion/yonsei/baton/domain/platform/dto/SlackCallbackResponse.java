package com.likelion.yonsei.baton.domain.platform.dto;

import com.likelion.yonsei.baton.domain.platform.entity.ConnectionStatus;

public record SlackCallbackResponse(
		Long id,
		String workspaceId,
		String workspaceName,
		ConnectionStatus connectionStatus
) {
}
