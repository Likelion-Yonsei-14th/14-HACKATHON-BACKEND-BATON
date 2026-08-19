package com.likelion.yonsei.baton.integration.slack.dto;

import java.util.List;

public record SlackConversationsListResponse(
		boolean ok,
		String error,
		List<SlackChannel> channels
) {

	/**
	 * is_channel/is_im/is_mpim are Boolean, not boolean — Slack omits (or nulls) whichever of these
	 * don't apply to a given conversation's type instead of sending an explicit false for each.
	 */
	public record SlackChannel(
			String id,
			String name,
			Boolean isChannel,
			Boolean isIm,
			Boolean isMpim,
			String user
	) {
	}
}
