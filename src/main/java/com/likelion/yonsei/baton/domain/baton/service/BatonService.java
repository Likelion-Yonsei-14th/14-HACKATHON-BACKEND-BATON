package com.likelion.yonsei.baton.domain.baton.service;

import com.likelion.yonsei.baton.common.exception.BusinessException;
import com.likelion.yonsei.baton.domain.baton.entity.Baton;
import com.likelion.yonsei.baton.domain.baton.entity.BatonStatus;
import com.likelion.yonsei.baton.domain.baton.exception.BatonErrorCode;
import com.likelion.yonsei.baton.domain.baton.repository.BatonRepository;
import com.likelion.yonsei.baton.domain.conversation.service.ConversationService;
import com.likelion.yonsei.baton.domain.message.entity.Message;
import com.likelion.yonsei.baton.domain.message.service.MessageService;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class BatonService {

	private static final int PAGE_SIZE = 20;

	private final BatonRepository batonRepository;
	private final ConversationService conversationService;
	private final MessageService messageService;
	private final Clock clock;

	public BatonService(
			BatonRepository batonRepository,
			ConversationService conversationService,
			MessageService messageService,
			Clock clock
	) {
		this.batonRepository = batonRepository;
		this.conversationService = conversationService;
		this.messageService = messageService;
		this.clock = clock;
	}

	@Transactional
	public Baton create(Long userId, Long conversationId, Long triggerMessageId, boolean autoSendEnabled, LocalDateTime expiresAt) {
		conversationService.getById(conversationId, userId);

		// Ownership-scoped lookup alone isn't enough here: a caller could own conversation A but pass
		// a trigger_message_id from their own conversation B (or, before this check existed, any
		// tenant's message once the underlying getById was scoped). Require it to actually be a
		// message of the conversation being linked.
		Message triggerMessage = messageService.getById(triggerMessageId, userId);
		if (!triggerMessage.getConversationId().equals(conversationId)) {
			throw new BusinessException(BatonErrorCode.TRIGGER_MESSAGE_NOT_IN_CONVERSATION);
		}

		if (batonRepository.existsByTriggerMessageId(triggerMessageId)) {
			throw new BusinessException(BatonErrorCode.TRIGGER_MESSAGE_ALREADY_USED);
		}
		Baton baton = new Baton(userId, conversationId, triggerMessageId, autoSendEnabled, expiresAt);
		return batonRepository.save(baton);
	}

	public List<Baton> search(Long userId, BatonStatus status, Long conversationId, Long cursor) {
		return batonRepository.search(userId, status, conversationId, cursor, PageRequest.of(0, PAGE_SIZE));
	}

	public Baton getById(Long id, Long userId) {
		return batonRepository.findByIdAndUserId(id, userId)
				.orElseThrow(() -> new BusinessException(BatonErrorCode.BATON_NOT_FOUND));
	}

	@Transactional
	public Baton update(Long id, Long userId, Boolean autoSendEnabled, LocalDateTime expiresAt) {
		Baton baton = getById(id, userId);
		baton.update(autoSendEnabled, expiresAt);
		return baton;
	}

	@Transactional
	public void delete(Long id, Long userId) {
		Baton baton = getById(id, userId);
		if (!baton.isDeletable()) {
			throw new BusinessException(BatonErrorCode.INVALID_STATUS_TRANSITION, "DRAFT, CANCELLED, EXPIRED 상태의 BATON만 삭제할 수 있습니다.");
		}
		batonRepository.delete(baton);
	}

	@Transactional
	public Baton activate(Long id, Long userId) {
		Baton baton = getById(id, userId);
		baton.activate(LocalDateTime.now(clock));
		return baton;
	}

	@Transactional
	public Baton cancel(Long id, Long userId) {
		Baton baton = getById(id, userId);
		baton.cancel();
		return baton;
	}
}
