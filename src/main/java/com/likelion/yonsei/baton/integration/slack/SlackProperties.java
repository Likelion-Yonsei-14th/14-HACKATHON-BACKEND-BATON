package com.likelion.yonsei.baton.integration.slack;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "slack")
public record SlackProperties(
		String clientId,
		String clientSecret,
		String signingSecret,
		String redirectUri
) {
}
