package com.likelion.yonsei.baton.integration.slack.dto;

import java.util.List;

public record SlackHistoryResponse(
		boolean ok,
		String error,
		List<SlackMessage> messages
) {

	public record SlackMessage(
			String ts,
			String user,
			String text,
			String type,
			String subtype
	) {
	}
}
