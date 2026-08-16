package com.likelion.yonsei.baton.domain.conversation.dto;

import com.likelion.yonsei.baton.domain.conversation.entity.Conversation;
import com.likelion.yonsei.baton.domain.conversation.entity.ConversationType;

public record ConversationResponse(
		Long id,
		Long platformConnectionId,
		String externalConversationId,
		String externalThreadId,
		ConversationType conversationType,
		String title,
		String counterpartExternalId,
		String counterpartName,
		String counterpartTimezone
) {

	public static ConversationResponse from(Conversation conversation) {
		return new ConversationResponse(
				conversation.getId(),
				conversation.getPlatformConnectionId(),
				conversation.getExternalConversationId(),
				conversation.getExternalThreadId(),
				conversation.getConversationType(),
				conversation.getTitle(),
				conversation.getCounterpartExternalId(),
				conversation.getCounterpartName(),
				conversation.getCounterpartTimezone()
		);
	}
}
