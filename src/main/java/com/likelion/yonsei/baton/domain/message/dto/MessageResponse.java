package com.likelion.yonsei.baton.domain.message.dto;

import com.likelion.yonsei.baton.domain.message.entity.Message;
import com.likelion.yonsei.baton.domain.message.entity.SenderType;

import java.time.LocalDateTime;

public record MessageResponse(
		Long id,
		Long conversationId,
		String externalMessageId,
		String senderExternalId,
		SenderType senderType,
		String content,
		String originalLanguage,
		boolean isBatonGenerated,
		LocalDateTime sentAt
) {

	public static MessageResponse from(Message message) {
		return new MessageResponse(
				message.getId(),
				message.getConversationId(),
				message.getExternalMessageId(),
				message.getSenderExternalId(),
				message.getSenderType(),
				message.getContent(),
				message.getOriginalLanguage(),
				message.isBatonGenerated(),
				message.getSentAt()
		);
	}
}
