package com.likelion.yonsei.baton.integration.slack;

import com.likelion.yonsei.baton.common.exception.BusinessException;
import com.likelion.yonsei.baton.domain.platform.exception.PlatformConnectionErrorCode;
import com.likelion.yonsei.baton.integration.slack.dto.SlackChatPostMessageResponse;
import com.likelion.yonsei.baton.integration.slack.dto.SlackConversationsListResponse;
import com.likelion.yonsei.baton.integration.slack.dto.SlackHistoryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;

/** Thin wrapper over the subset of Slack Web API endpoints BATON needs. */
@Component
public class SlackApiClient {

	private static final Logger log = LoggerFactory.getLogger(SlackApiClient.class);

	private final RestClient slackRestClient;

	public SlackApiClient(RestClient slackRestClient) {
		this.slackRestClient = slackRestClient;
	}

	public SlackConversationsListResponse listConversations(String accessToken) {
		SlackConversationsListResponse response = get(
				accessToken,
				"/conversations.list?types=public_channel,private_channel,im,mpim&limit=200",
				SlackConversationsListResponse.class
		);
		if (!response.ok()) {
			log.error("Slack conversations.list failed: {}", response.error());
			throw new BusinessException(PlatformConnectionErrorCode.SLACK_API_FAILED);
		}
		return response;
	}

	/** Returns null on any failure — a missing display name shouldn't fail the whole conversations sync. */
	public SlackUserInfoResponse getUserInfo(String accessToken, String userId) {
		try {
			SlackUserInfoResponse response = get(accessToken, "/users.info?user=%s".formatted(userId), SlackUserInfoResponse.class);
			return response.ok() ? response : null;
		} catch (Exception e) {
			log.warn("Slack users.info failed for user={}", userId, e);
			return null;
		}
	}

	public SlackHistoryResponse fetchHistory(String accessToken, String channelId, int limit) {
		SlackHistoryResponse response = get(
				accessToken,
				"/conversations.history?channel=%s&limit=%d".formatted(channelId, limit),
				SlackHistoryResponse.class
		);
		if (!response.ok()) {
			log.error("Slack conversations.history failed: {}", response.error());
			throw new BusinessException(PlatformConnectionErrorCode.SLACK_API_FAILED);
		}
		return response;
	}

	public SlackChatPostMessageResponse postMessage(String accessToken, String channelId, String text) {
		SlackChatPostMessageResponse response;
		try {
			response = slackRestClient.post()
					.uri("/chat.postMessage")
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
					.body(Map.of("channel", channelId, "text", text))
					.retrieve()
					.body(SlackChatPostMessageResponse.class);
		} catch (RestClientException e) {
			log.error("Slack chat.postMessage failed", e);
			throw new BusinessException(PlatformConnectionErrorCode.SLACK_API_FAILED);
		}
		if (response == null || !response.ok()) {
			log.error("Slack chat.postMessage rejected: {}", response == null ? "empty response" : response.error());
			throw new BusinessException(PlatformConnectionErrorCode.SLACK_API_FAILED);
		}
		return response;
	}

	private <T> T get(String accessToken, String uri, Class<T> type) {
		try {
			T body = slackRestClient.get()
					.uri(uri)
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
					.retrieve()
					.body(type);
			if (body == null) {
				throw new BusinessException(PlatformConnectionErrorCode.SLACK_API_FAILED);
			}
			return body;
		} catch (RestClientException e) {
			log.error("Slack API request failed: {}", uri, e);
			throw new BusinessException(PlatformConnectionErrorCode.SLACK_API_FAILED);
		}
	}
}
