package com.likelion.yonsei.baton.domain.message.service;

import com.likelion.yonsei.baton.common.crypto.TokenEncryptor;
import com.likelion.yonsei.baton.common.exception.BusinessException;
import com.likelion.yonsei.baton.domain.conversation.entity.Conversation;
import com.likelion.yonsei.baton.domain.conversation.service.ConversationService;
import com.likelion.yonsei.baton.domain.message.entity.Message;
import com.likelion.yonsei.baton.domain.message.entity.SenderType;
import com.likelion.yonsei.baton.domain.message.exception.MessageErrorCode;
import com.likelion.yonsei.baton.domain.message.repository.MessageRepository;
import com.likelion.yonsei.baton.domain.platform.entity.PlatformConnection;
import com.likelion.yonsei.baton.domain.platform.exception.PlatformConnectionErrorCode;
import com.likelion.yonsei.baton.domain.platform.repository.PlatformConnectionRepository;
import com.likelion.yonsei.baton.integration.slack.SlackApiClient;
import com.likelion.yonsei.baton.integration.slack.dto.SlackChatPostMessageResponse;
import com.likelion.yonsei.baton.integration.slack.dto.SlackHistoryResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class MessageService {

	private static final int DEFAULT_LIST_LIMIT = 20;
	private static final int DEFAULT_SYNC_LIMIT = 20;

	private final MessageRepository messageRepository;
	private final ConversationService conversationService;
	private final PlatformConnectionRepository platformConnectionRepository;
	private final SlackApiClient slackApiClient;
	private final TokenEncryptor tokenEncryptor;
	private final Clock clock;

	public MessageService(
			MessageRepository messageRepository,
			ConversationService conversationService,
			PlatformConnectionRepository platformConnectionRepository,
			SlackApiClient slackApiClient,
			TokenEncryptor tokenEncryptor,
			Clock clock
	) {
		this.messageRepository = messageRepository;
		this.conversationService = conversationService;
		this.platformConnectionRepository = platformConnectionRepository;
		this.slackApiClient = slackApiClient;
		this.tokenEncryptor = tokenEncryptor;
		this.clock = clock;
	}

	public List<Message> list(Long conversationId, Long userId, Integer limit, LocalDateTime before) {
		conversationService.getById(conversationId, userId);
		int pageSize = limit != null ? limit : DEFAULT_LIST_LIMIT;
		if (before != null) {
			return messageRepository.findByConversationIdAndSentAtLessThanOrderBySentAtDesc(
					conversationId, before, PageRequest.of(0, pageSize));
		}
		return messageRepository.findByConversationIdOrderBySentAtDesc(conversationId, PageRequest.of(0, pageSize));
	}

	public Message getById(Long id, Long userId) {
		return messageRepository.findByIdAndUserId(id, userId)
				.orElseThrow(() -> new BusinessException(MessageErrorCode.MESSAGE_NOT_FOUND));
	}

	@Transactional
	public Message send(Long conversationId, Long userId, String content) {
		Conversation conversation = conversationService.getById(conversationId, userId);
		PlatformConnection connection = platformConnectionRepository.findById(conversation.getPlatformConnectionId())
				.orElseThrow(() -> new BusinessException(PlatformConnectionErrorCode.CONNECTION_NOT_FOUND));

		String accessToken = tokenEncryptor.decrypt(connection.getAccessTokenEncrypted());
		SlackChatPostMessageResponse response = slackApiClient.postMessage(
				accessToken, conversation.getExternalConversationId(), content);

		LocalDateTime sentAt = LocalDateTime.now(clock);
		Message message = new Message(
				conversationId,
				response.ts(),
				null,
				connection.getWorkspaceId(),
				SenderType.USER,
				content,
				null,
				false,
				sentAt
		);
		return messageRepository.save(message);
	}

	@Transactional
	public MessageSyncResult sync(Long conversationId, Long userId, Integer limit) {
		Conversation conversation = conversationService.getById(conversationId, userId);
		PlatformConnection connection = platformConnectionRepository.findById(conversation.getPlatformConnectionId())
				.orElseThrow(() -> new BusinessException(PlatformConnectionErrorCode.CONNECTION_NOT_FOUND));

		String accessToken = tokenEncryptor.decrypt(connection.getAccessTokenEncrypted());
		int pageSize = limit != null ? limit : DEFAULT_SYNC_LIMIT;
		SlackHistoryResponse history = slackApiClient.fetchHistory(accessToken, conversation.getExternalConversationId(), pageSize);

		int syncedCount = 0;
		Long lastMessageId = null;
		for (SlackHistoryResponse.SlackMessage slackMessage : history.messages()) {
			if (messageRepository.findByConversationIdAndExternalMessageId(conversationId, slackMessage.ts()).isPresent()) {
				continue;
			}
			LocalDateTime sentAt = LocalDateTime.ofInstant(
					Instant.ofEpochMilli((long) (Double.parseDouble(slackMessage.ts()) * 1000)), ZoneOffset.UTC);
			boolean isBot = "bot_message".equals(slackMessage.subtype());
			Message message = new Message(
					conversationId,
					slackMessage.ts(),
					null,
					slackMessage.user(),
					isBot ? SenderType.BATON : SenderType.COUNTERPART,
					slackMessage.text(),
					null,
					isBot,
					sentAt
			);
			Message saved = messageRepository.save(message);
			lastMessageId = saved.getId();
			syncedCount++;
		}

		return new MessageSyncResult(syncedCount, lastMessageId, LocalDateTime.now(clock));
	}

	public record MessageSyncResult(int syncedCount, Long lastMessageId, LocalDateTime lastSyncedAt) {
	}
}
