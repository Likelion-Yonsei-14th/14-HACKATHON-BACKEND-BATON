package com.likelion.yonsei.baton.domain.conversation.dto;

import com.likelion.yonsei.baton.domain.conversation.entity.Conversation;
import com.likelion.yonsei.baton.domain.conversation.entity.ConversationType;

public record ConversationSummaryResponse(
		Long id,
		Long platformConnectionId,
		String externalConversationId,
		ConversationType conversationType,
		String title,
		String counterpartName,
		String counterpartTimezone
) {

	public static ConversationSummaryResponse from(Conversation conversation) {
		return new ConversationSummaryResponse(
				conversation.getId(),
				conversation.getPlatformConnectionId(),
				conversation.getExternalConversationId(),
				conversation.getConversationType(),
				conversation.getTitle(),
				conversation.getCounterpartName(),
				conversation.getCounterpartTimezone()
		);
	}
}
