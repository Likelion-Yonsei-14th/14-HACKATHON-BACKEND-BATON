package com.likelion.yonsei.baton.integration.slack.dto;

public record SlackEventPayload(
		String type,
		String eventId,
		String challenge,
		SlackEvent event
) {

	public record SlackEvent(
			String type,
			String subtype,
			String channel,
			String user,
			String text,
			String ts,
			String threadTs
	) {
	}
}
