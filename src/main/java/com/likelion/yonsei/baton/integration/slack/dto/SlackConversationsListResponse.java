package com.likelion.yonsei.baton.integration.slack.dto;

import java.util.List;

public record SlackConversationsListResponse(
		boolean ok,
		String error,
		List<SlackChannel> channels
) {

	public record SlackChannel(
			String id,
			String name,
			boolean isChannel,
			boolean isIm,
			boolean isMpim,
			String user
	) {
	}
}
