package com.likelion.yonsei.baton.integration.slack;

import com.likelion.yonsei.baton.common.exception.BusinessException;
import com.likelion.yonsei.baton.common.response.ApiResponse;
import com.likelion.yonsei.baton.integration.slack.dto.SlackEventPayload;
import com.likelion.yonsei.baton.integration.slack.exception.SlackWebhookErrorCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

@RestController
@RequestMapping("/api/webhooks/slack")
public class SlackWebhookController {

	private final SlackSignatureVerifier signatureVerifier;
	private final SlackWebhookService webhookService;
	private final ObjectMapper objectMapper;

	public SlackWebhookController(SlackSignatureVerifier signatureVerifier, SlackWebhookService webhookService, ObjectMapper objectMapper) {
		this.signatureVerifier = signatureVerifier;
		this.webhookService = webhookService;
		this.objectMapper = objectMapper;
	}

	@PostMapping("/events")
	public ResponseEntity<?> receiveEvent(
			@RequestHeader(value = "X-Slack-Request-Timestamp", required = false) String timestamp,
			@RequestHeader(value = "X-Slack-Signature", required = false) String signature,
			@RequestBody String rawBody
	) {
		if (!signatureVerifier.isValid(timestamp, signature, rawBody)) {
			throw new BusinessException(SlackWebhookErrorCode.INVALID_SLACK_SIGNATURE);
		}

		SlackEventPayload payload = parse(rawBody);

		if ("url_verification".equals(payload.type())) {
			return ResponseEntity.ok(Map.of("challenge", payload.challenge()));
		}

		if ("event_callback".equals(payload.type())) {
			webhookService.handleEventCallback(payload);
		}

		return ResponseEntity.ok(ApiResponse.success(Map.of("ok", true)));
	}

	private SlackEventPayload parse(String rawBody) {
		try {
			return objectMapper.readValue(rawBody, SlackEventPayload.class);
		} catch (Exception e) {
			throw new BusinessException(SlackWebhookErrorCode.SLACK_EVENT_PROCESSING_FAILED);
		}
	}
}
