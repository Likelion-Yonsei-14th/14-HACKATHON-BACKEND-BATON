package com.likelion.yonsei.baton.integration.slack.dto;

public record SlackChatPostMessageResponse(
		boolean ok,
		String error,
		String channel,
		String ts
) {
}
