package com.likelion.yonsei.baton.domain.conversation.service;

import com.likelion.yonsei.baton.common.exception.BusinessException;
import com.likelion.yonsei.baton.domain.conversation.entity.Conversation;
import com.likelion.yonsei.baton.domain.conversation.entity.ConversationType;
import com.likelion.yonsei.baton.domain.conversation.exception.ConversationErrorCode;
import com.likelion.yonsei.baton.domain.conversation.repository.ConversationRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ConversationService {

	private static final int PAGE_SIZE = 20;

	private final ConversationRepository conversationRepository;

	public ConversationService(ConversationRepository conversationRepository) {
		this.conversationRepository = conversationRepository;
	}

	public List<Conversation> search(Long userId, Long platformConnectionId, ConversationType type, Long cursor) {
		return conversationRepository.search(userId, platformConnectionId, type, cursor, PageRequest.of(0, PAGE_SIZE));
	}

	public Conversation getById(Long id, Long userId) {
		return conversationRepository.findByIdAndUserId(id, userId)
				.orElseThrow(() -> new BusinessException(ConversationErrorCode.CONVERSATION_NOT_FOUND));
	}

	public Conversation getByIdInternal(Long id) {
		return conversationRepository.findById(id)
				.orElseThrow(() -> new BusinessException(ConversationErrorCode.CONVERSATION_NOT_FOUND));
	}
}
