package com.likelion.yonsei.baton.integration.slack;

import com.likelion.yonsei.baton.common.exception.BusinessException;
import com.likelion.yonsei.baton.domain.baton.service.ReplyProcessingService;
import com.likelion.yonsei.baton.domain.conversation.entity.Conversation;
import com.likelion.yonsei.baton.domain.conversation.repository.ConversationRepository;
import com.likelion.yonsei.baton.domain.message.entity.Message;
import com.likelion.yonsei.baton.domain.message.entity.SenderType;
import com.likelion.yonsei.baton.domain.message.repository.MessageRepository;
import com.likelion.yonsei.baton.integration.slack.dto.SlackEventPayload;
import com.likelion.yonsei.baton.integration.slack.exception.SlackWebhookErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class SlackWebhookService {

	private static final Logger log = LoggerFactory.getLogger(SlackWebhookService.class);

	private final ConversationRepository conversationRepository;
	private final MessageRepository messageRepository;
	private final ReplyProcessingService replyProcessingService;
	private final Clock clock;

	public SlackWebhookService(
			ConversationRepository conversationRepository,
			MessageRepository messageRepository,
			ReplyProcessingService replyProcessingService,
			Clock clock
	) {
		this.conversationRepository = conversationRepository;
		this.messageRepository = messageRepository;
		this.replyProcessingService = replyProcessingService;
		this.clock = clock;
	}

	@Transactional
	public void handleEventCallback(SlackEventPayload payload) {
		if (payload.eventId() != null && messageRepository.existsByExternalEventId(payload.eventId())) {
			log.info("Ignoring duplicate Slack event_id={}", payload.eventId());
			return;
		}

		SlackEventPayload.SlackEvent event = payload.event();
		if (event == null || !"message".equals(event.type()) || event.text() == null) {
			return;
		}
		// bot_message subtype covers BATON's own sends, which are saved directly by ActionExecutor already.
		if ("bot_message".equals(event.subtype())) {
			return;
		}

		Conversation conversation = conversationRepository.findFirstByExternalConversationId(event.channel())
				.orElse(null);
		if (conversation == null) {
			log.warn("Slack event for unknown channel={}, ignoring", event.channel());
			return;
		}

		LocalDateTime sentAt = event.ts() != null ? toLocalDateTime(event.ts()) : LocalDateTime.now(clock);
		Message message = new Message(
				conversation.getId(),
				event.ts(),
				payload.eventId(),
				event.user(),
				SenderType.COUNTERPART,
				event.text(),
				null,
				false,
				sentAt
		);

		Message saved;
		try {
			saved = messageRepository.save(message);
		} catch (Exception e) {
			log.error("Failed to save Slack event message", e);
			throw new BusinessException(SlackWebhookErrorCode.SLACK_EVENT_PROCESSING_FAILED);
		}

		replyProcessingService.process(conversation.getId(), saved);
	}

	private LocalDateTime toLocalDateTime(String slackTs) {
		try {
			return LocalDateTime.ofInstant(Instant.ofEpochMilli((long) (Double.parseDouble(slackTs) * 1000)), ZoneOffset.UTC);
		} catch (NumberFormatException e) {
			return LocalDateTime.now(clock);
		}
	}
}
