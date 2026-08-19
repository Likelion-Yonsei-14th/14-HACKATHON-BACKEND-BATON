package com.likelion.yonsei.baton.integration.slack;

import com.likelion.yonsei.baton.common.exception.BusinessException;
import com.likelion.yonsei.baton.domain.platform.exception.PlatformConnectionErrorCode;
import com.likelion.yonsei.baton.integration.slack.dto.SlackOAuthTokenResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

/** Slack OAuth v2 authorization-code exchange. Requires a real Slack app (SLACK_CLIENT_ID/SECRET) to function end to end. */
@Component
public class SlackOAuthClient {

	private static final Logger log = LoggerFactory.getLogger(SlackOAuthClient.class);
	private static final String USER_SCOPES =
			"channels:history,channels:read,chat:write,groups:history,groups:read,im:history,im:read,mpim:history,mpim:read";

	private final RestClient slackRestClient;
	private final SlackProperties properties;

	public SlackOAuthClient(RestClient slackRestClient, SlackProperties properties) {
		this.slackRestClient = slackRestClient;
		this.properties = properties;
	}

	public String buildAuthorizeUrl(String state) {
		return UriComponentsBuilder.fromUriString("https://slack.com/oauth/v2/authorize")
				.queryParam("client_id", properties.clientId())
				.queryParam("user_scope", USER_SCOPES)
				.queryParam("redirect_uri", properties.redirectUri())
				.queryParam("state", state)
				.build()
				.toUriString();
	}

	public SlackOAuthTokenResponse exchangeCode(String code) {
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("client_id", properties.clientId());
		form.add("client_secret", properties.clientSecret());
		form.add("code", code);
		form.add("redirect_uri", properties.redirectUri());

		SlackOAuthTokenResponse response;
		try {
			response = slackRestClient.post()
					.uri("/oauth.v2.access")
					.body(form)
					.retrieve()
					.body(SlackOAuthTokenResponse.class);
		} catch (RestClientException e) {
			log.error("Slack OAuth token exchange failed", e);
			throw new BusinessException(PlatformConnectionErrorCode.SLACK_OAUTH_FAILED);
		}

		if (response == null || !response.ok()) {
			log.error("Slack OAuth token exchange rejected: {}", response == null ? "empty response" : response.error());
			throw new BusinessException(PlatformConnectionErrorCode.SLACK_OAUTH_FAILED);
		}
		return response;
	}
}
