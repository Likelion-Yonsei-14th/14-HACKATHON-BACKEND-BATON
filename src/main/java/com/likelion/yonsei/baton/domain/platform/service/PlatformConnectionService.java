package com.likelion.yonsei.baton.domain.platform.service;

import com.likelion.yonsei.baton.common.crypto.TokenEncryptor;
import com.likelion.yonsei.baton.common.exception.BusinessException;
import com.likelion.yonsei.baton.domain.conversation.entity.Conversation;
import com.likelion.yonsei.baton.domain.conversation.entity.ConversationType;
import com.likelion.yonsei.baton.domain.conversation.repository.ConversationRepository;
import com.likelion.yonsei.baton.domain.platform.entity.PlatformConnection;
import com.likelion.yonsei.baton.domain.platform.entity.PlatformType;
import com.likelion.yonsei.baton.domain.platform.exception.PlatformConnectionErrorCode;
import com.likelion.yonsei.baton.domain.platform.repository.PlatformConnectionRepository;
import com.likelion.yonsei.baton.domain.platform.support.SlackOAuthStateStore;
import com.likelion.yonsei.baton.integration.slack.SlackApiClient;
import com.likelion.yonsei.baton.integration.slack.SlackOAuthClient;
import com.likelion.yonsei.baton.integration.slack.dto.SlackConversationsListResponse;
import com.likelion.yonsei.baton.integration.slack.dto.SlackOAuthTokenResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class PlatformConnectionService {

	private final PlatformConnectionRepository platformConnectionRepository;
	private final ConversationRepository conversationRepository;
	private final SlackOAuthClient slackOAuthClient;
	private final SlackApiClient slackApiClient;
	private final SlackOAuthStateStore stateStore;
	private final TokenEncryptor tokenEncryptor;
	private final Clock clock;

	public PlatformConnectionService(
			PlatformConnectionRepository platformConnectionRepository,
			ConversationRepository conversationRepository,
			SlackOAuthClient slackOAuthClient,
			SlackApiClient slackApiClient,
			SlackOAuthStateStore stateStore,
			TokenEncryptor tokenEncryptor,
			Clock clock
	) {
		this.platformConnectionRepository = platformConnectionRepository;
		this.conversationRepository = conversationRepository;
		this.slackOAuthClient = slackOAuthClient;
		this.slackApiClient = slackApiClient;
		this.stateStore = stateStore;
		this.tokenEncryptor = tokenEncryptor;
		this.clock = clock;
	}

	public List<PlatformConnection> list(Long userId) {
		return platformConnectionRepository.findByUserId(userId);
	}

	public PlatformConnection getById(Long id, Long userId) {
		return platformConnectionRepository.findByIdAndUserId(id, userId)
				.orElseThrow(() -> new BusinessException(PlatformConnectionErrorCode.CONNECTION_NOT_FOUND));
	}

	@Transactional
	public PlatformConnection disconnect(Long id, Long userId) {
		PlatformConnection connection = getById(id, userId);
		connection.disconnect();
		return connection;
	}

	public String startSlackConnect(Long userId) {
		String state = stateStore.issue(userId);
		return slackOAuthClient.buildAuthorizeUrl(state);
	}

	@Transactional
	public PlatformConnection handleSlackCallback(String code, String state) {
		Long userId = stateStore.consume(state);
		if (userId == null) {
			throw new BusinessException(PlatformConnectionErrorCode.INVALID_OAUTH_STATE);
		}

		SlackOAuthTokenResponse token = slackOAuthClient.exchangeCode(code);
		String workspaceId = token.team() != null ? token.team().id() : null;
		String workspaceName = token.team() != null ? token.team().name() : null;
		String accessToken = token.authedUser() != null && token.authedUser().accessToken() != null
				? token.authedUser().accessToken()
				: token.accessToken();

		LocalDateTime expiresAt = token.expiresIn() != null
				? LocalDateTime.now(clock).plusSeconds(token.expiresIn())
				: null;

		PlatformConnection connection = new PlatformConnection(
				userId,
				PlatformType.SLACK,
				workspaceId,
				workspaceName,
				tokenEncryptor.encrypt(accessToken),
				token.refreshToken() != null ? tokenEncryptor.encrypt(token.refreshToken()) : null,
				expiresAt
		);
		return platformConnectionRepository.save(connection);
	}

	@Transactional
	public ConversationsSyncResult syncConversations(Long connectionId, Long userId) {
		PlatformConnection connection = getById(connectionId, userId);
		String accessToken = tokenEncryptor.decrypt(connection.getAccessTokenEncrypted());

		SlackConversationsListResponse response;
		try {
			response = slackApiClient.listConversations(accessToken);
		} catch (BusinessException e) {
			connection.markError();
			throw e;
		}

		int created = 0;
		int updated = 0;
		for (SlackConversationsListResponse.SlackChannel channel : response.channels()) {
			ConversationType type = channel.isIm() || channel.isMpim() ? ConversationType.DM : ConversationType.CHANNEL;
			var existing = conversationRepository.findByPlatformConnectionIdAndExternalConversationIdAndExternalThreadId(
					connection.getId(), channel.id(), null);
			if (existing.isPresent()) {
				updated++;
				continue;
			}
			Conversation conversation = new Conversation(
					connection.getId(),
					channel.id(),
					null,
					type,
					channel.name(),
					channel.user(),
					null,
					null
			);
			conversationRepository.save(conversation);
			created++;
		}

		LocalDateTime now = LocalDateTime.now(clock);
		connection.markSynced(now);
		return new ConversationsSyncResult(created, updated, now);
	}

	public record ConversationsSyncResult(int createdCount, int updatedCount, LocalDateTime lastSyncedAt) {
	}
}
