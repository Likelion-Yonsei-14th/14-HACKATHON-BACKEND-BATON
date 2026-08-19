package com.likelion.yonsei.baton.integration.slack;

import com.likelion.yonsei.baton.common.crypto.TokenEncryptor;
import com.likelion.yonsei.baton.domain.baton.entity.Baton;
import com.likelion.yonsei.baton.domain.baton.entity.BatonStatus;
import com.likelion.yonsei.baton.domain.baton.repository.BatonRepository;
import com.likelion.yonsei.baton.domain.baton.service.ReplyProcessingService;
import com.likelion.yonsei.baton.domain.conversation.entity.Conversation;
import com.likelion.yonsei.baton.domain.conversation.repository.ConversationRepository;
import com.likelion.yonsei.baton.domain.message.entity.Message;
import com.likelion.yonsei.baton.domain.message.entity.SenderType;
import com.likelion.yonsei.baton.domain.message.repository.MessageRepository;
import com.likelion.yonsei.baton.domain.platform.entity.ConnectionStatus;
import com.likelion.yonsei.baton.domain.platform.entity.PlatformConnection;
import com.likelion.yonsei.baton.domain.platform.repository.PlatformConnectionRepository;
import com.likelion.yonsei.baton.integration.slack.dto.SlackHistoryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Slack's Events API needs an HTTPS callback URL, which this deployment doesn't have — so instead
 * of waiting for a push, this periodically pulls recent history for every conversation with a
 * WAITING baton and feeds any message we haven't seen yet through the same pipeline
 * {@link com.likelion.yonsei.baton.integration.slack.SlackWebhookService} uses for pushed events.
 */
@Service
public class SlackPollingService {

	private static final Logger log = LoggerFactory.getLogger(SlackPollingService.class);
	private static final int HISTORY_LIMIT = 20;

	private final BatonRepository batonRepository;
	private final ConversationRepository conversationRepository;
	private final PlatformConnectionRepository platformConnectionRepository;
	private final MessageRepository messageRepository;
	private final SlackApiClient slackApiClient;
	private final TokenEncryptor tokenEncryptor;
	private final ReplyProcessingService replyProcessingService;
	private final Clock clock;

	public SlackPollingService(
			BatonRepository batonRepository,
			ConversationRepository conversationRepository,
			PlatformConnectionRepository platformConnectionRepository,
			MessageRepository messageRepository,
			SlackApiClient slackApiClient,
			TokenEncryptor tokenEncryptor,
			ReplyProcessingService replyProcessingService,
			Clock clock
	) {
		this.batonRepository = batonRepository;
		this.conversationRepository = conversationRepository;
		this.platformConnectionRepository = platformConnectionRepository;
		this.messageRepository = messageRepository;
		this.slackApiClient = slackApiClient;
		this.tokenEncryptor = tokenEncryptor;
		this.replyProcessingService = replyProcessingService;
		this.clock = clock;
	}

	@Scheduled(fixedDelay = 30_000, initialDelay = 15_000)
	@Transactional
	public void pollWaitingBatons() {
		List<Baton> waiting = batonRepository.findByStatus(BatonStatus.WAITING);
		for (Baton baton : waiting) {
			try {
				pollOne(baton);
			} catch (Exception e) {
				log.warn("Slack polling failed for baton={}", baton.getId(), e);
			}
		}
	}

	private void pollOne(Baton baton) {
		Conversation conversation = conversationRepository.findById(baton.getConversationId()).orElse(null);
		if (conversation == null) {
			return;
		}
		PlatformConnection connection = platformConnectionRepository.findById(conversation.getPlatformConnectionId())
				.orElse(null);
		if (connection == null || connection.getConnectionStatus() != ConnectionStatus.CONNECTED) {
			return;
		}

		String accessToken = tokenEncryptor.decrypt(connection.getAccessTokenEncrypted());
		SlackHistoryResponse history = slackApiClient.fetchHistory(accessToken, conversation.getExternalConversationId(), HISTORY_LIMIT);

		// Slack returns newest-first; replay oldest-first so a burst of messages is classified in order.
		List<SlackHistoryResponse.SlackMessage> messages = new ArrayList<>(history.messages());
		Collections.reverse(messages);
		for (SlackHistoryResponse.SlackMessage slackMessage : messages) {
			if (slackMessage.ts() == null || slackMessage.text() == null) {
				continue;
			}
			// bot_message subtype covers BATON's own sends, which are saved directly by ActionExecutor already.
			if ("bot_message".equals(slackMessage.subtype())) {
				continue;
			}
			if (messageRepository.findByConversationIdAndExternalMessageId(conversation.getId(), slackMessage.ts()).isPresent()) {
				continue;
			}

			Message saved = messageRepository.save(new Message(
					conversation.getId(),
					slackMessage.ts(),
					null,
					slackMessage.user(),
					SenderType.COUNTERPART,
					slackMessage.text(),
					null,
					false,
					toLocalDateTime(slackMessage.ts())
			));
			replyProcessingService.process(conversation.getId(), saved);
		}
	}

	private LocalDateTime toLocalDateTime(String slackTs) {
		try {
			return LocalDateTime.ofInstant(Instant.ofEpochMilli((long) (Double.parseDouble(slackTs) * 1000)), ZoneOffset.UTC);
		} catch (NumberFormatException e) {
			return LocalDateTime.now(clock);
		}
	}
}
