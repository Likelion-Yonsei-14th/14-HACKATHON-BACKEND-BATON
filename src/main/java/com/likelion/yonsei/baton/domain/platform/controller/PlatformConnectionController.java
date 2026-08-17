package com.likelion.yonsei.baton.domain.platform.controller;

import com.likelion.yonsei.baton.common.exception.BusinessException;
import com.likelion.yonsei.baton.common.response.ApiResponse;
import com.likelion.yonsei.baton.common.web.CurrentUserId;
import com.likelion.yonsei.baton.domain.platform.dto.ConversationsSyncResponse;
import com.likelion.yonsei.baton.domain.platform.dto.PlatformConnectionDisconnectResponse;
import com.likelion.yonsei.baton.domain.platform.dto.PlatformConnectionResponse;
import com.likelion.yonsei.baton.domain.platform.dto.PlatformConnectionSummaryResponse;
import com.likelion.yonsei.baton.domain.platform.dto.SlackConnectResponse;
import com.likelion.yonsei.baton.domain.platform.entity.PlatformConnection;
import com.likelion.yonsei.baton.domain.platform.service.PlatformConnectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/platform-connections")
public class PlatformConnectionController {

	private static final Logger log = LoggerFactory.getLogger(PlatformConnectionController.class);
	private static final String CALLBACK_PATH = "/connect/callback";

	private final PlatformConnectionService platformConnectionService;
	private final String frontendUrl;

	public PlatformConnectionController(
			PlatformConnectionService platformConnectionService,
			@Value("${app.frontend-url:http://localhost:5173}") String frontendUrl
	) {
		this.platformConnectionService = platformConnectionService;
		this.frontendUrl = frontendUrl;
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

	/**
	 * Slack redirects the user's browser here after they approve/deny the OAuth consent screen, so
	 * this must end in a browser redirect back to the frontend rather than a JSON body — there is no
	 * frontend code running on this response to read JSON from. The frontend then re-fetches
	 * GET /platform-connections to pick up the new connection, so query params are a nice-to-have,
	 * not load-bearing.
	 */
	@GetMapping("/slack/callback")
	public ResponseEntity<Void> slackCallback(
			@RequestParam String code,
			@RequestParam String state
	) {
		String status = "success";
		try {
			platformConnectionService.handleSlackCallback(code, state);
		} catch (BusinessException e) {
			log.warn("Slack OAuth callback failed: code={}", e.getErrorCode().getCode());
			status = "error";
		}

		URI location = UriComponentsBuilder.fromUriString(frontendUrl)
				.path(CALLBACK_PATH)
				.queryParam("status", status)
				.build()
				.toUri();
		return ResponseEntity.status(HttpStatus.FOUND).header(HttpHeaders.LOCATION, location.toString()).build();
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
