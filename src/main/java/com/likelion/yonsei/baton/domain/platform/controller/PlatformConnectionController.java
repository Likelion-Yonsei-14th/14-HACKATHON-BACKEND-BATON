package com.likelion.yonsei.baton.domain.platform.controller;

import com.likelion.yonsei.baton.common.response.ApiResponse;
import com.likelion.yonsei.baton.common.web.CurrentUserId;
import com.likelion.yonsei.baton.domain.platform.dto.ConversationsSyncResponse;
import com.likelion.yonsei.baton.domain.platform.dto.PlatformConnectionDisconnectResponse;
import com.likelion.yonsei.baton.domain.platform.dto.PlatformConnectionResponse;
import com.likelion.yonsei.baton.domain.platform.dto.PlatformConnectionSummaryResponse;
import com.likelion.yonsei.baton.domain.platform.dto.SlackCallbackResponse;
import com.likelion.yonsei.baton.domain.platform.dto.SlackConnectResponse;
import com.likelion.yonsei.baton.domain.platform.entity.PlatformConnection;
import com.likelion.yonsei.baton.domain.platform.service.PlatformConnectionService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/platform-connections")
public class PlatformConnectionController {

	private final PlatformConnectionService platformConnectionService;

	public PlatformConnectionController(PlatformConnectionService platformConnectionService) {
		this.platformConnectionService = platformConnectionService;
	}

	@GetMapping
	public ApiResponse<List<PlatformConnectionSummaryResponse>> list(@CurrentUserId Long userId) {
		List<PlatformConnectionSummaryResponse> connections = platformConnectionService.list(userId).stream()
				.map(PlatformConnectionSummaryResponse::from)
				.toList();
		return ApiResponse.success(connections);
	}

	@GetMapping("/slack/connect")
	public ApiResponse<SlackConnectResponse> startSlackConnect(@CurrentUserId Long userId) {
		String redirectUrl = platformConnectionService.startSlackConnect(userId);
		return ApiResponse.success(new SlackConnectResponse(redirectUrl));
	}

	@GetMapping("/slack/callback")
	public ApiResponse<SlackCallbackResponse> slackCallback(
			@RequestParam String code,
			@RequestParam String state
	) {
		PlatformConnection connection = platformConnectionService.handleSlackCallback(code, state);
		return ApiResponse.success(new SlackCallbackResponse(
				connection.getId(),
				connection.getWorkspaceId(),
				connection.getWorkspaceName(),
				connection.getConnectionStatus()
		));
	}

	@GetMapping("/{id}")
	public ApiResponse<PlatformConnectionResponse> getById(@CurrentUserId Long userId, @PathVariable Long id) {
		PlatformConnection connection = platformConnectionService.getById(id, userId);
		return ApiResponse.success(PlatformConnectionResponse.from(connection));
	}

	@DeleteMapping("/{id}")
	public ApiResponse<PlatformConnectionDisconnectResponse> disconnect(@CurrentUserId Long userId, @PathVariable Long id) {
		PlatformConnection connection = platformConnectionService.disconnect(id, userId);
		return ApiResponse.success(new PlatformConnectionDisconnectResponse(
				connection.getId(),
				connection.getConnectionStatus(),
				connection.getUpdatedAt()
		));
	}

	@PostMapping("/{connectionId}/conversations/sync")
	public ApiResponse<ConversationsSyncResponse> syncConversations(@CurrentUserId Long userId, @PathVariable Long connectionId) {
		PlatformConnectionService.ConversationsSyncResult result = platformConnectionService.syncConversations(connectionId, userId);
		return ApiResponse.success(new ConversationsSyncResponse(
				connectionId,
				result.createdCount(),
				result.updatedCount(),
				result.lastSyncedAt()
		));
	}
}
