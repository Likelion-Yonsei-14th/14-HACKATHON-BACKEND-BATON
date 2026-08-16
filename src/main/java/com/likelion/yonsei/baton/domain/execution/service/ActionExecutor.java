package com.likelion.yonsei.baton.domain.execution.service;

import com.likelion.yonsei.baton.common.crypto.TokenEncryptor;
import com.likelion.yonsei.baton.common.exception.BusinessException;
import com.likelion.yonsei.baton.domain.baton.branch.entity.ActionType;
import com.likelion.yonsei.baton.domain.baton.branch.entity.Branch;
import com.likelion.yonsei.baton.domain.baton.entity.Baton;
import com.likelion.yonsei.baton.domain.conversation.entity.Conversation;
import com.likelion.yonsei.baton.domain.conversation.repository.ConversationRepository;
import com.likelion.yonsei.baton.domain.execution.entity.Execution;
import com.likelion.yonsei.baton.domain.execution.repository.ExecutionRepository;
import com.likelion.yonsei.baton.domain.message.entity.Message;
import com.likelion.yonsei.baton.domain.message.entity.SenderType;
import com.likelion.yonsei.baton.domain.message.repository.MessageRepository;
import com.likelion.yonsei.baton.domain.platform.entity.PlatformConnection;
import com.likelion.yonsei.baton.domain.platform.exception.PlatformConnectionErrorCode;
import com.likelion.yonsei.baton.domain.platform.repository.PlatformConnectionRepository;
import com.likelion.yonsei.baton.integration.slack.SlackApiClient;
import com.likelion.yonsei.baton.integration.slack.dto.SlackChatPostMessageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

/**
 * Executes only rule-engine-approved actions. Never invoked directly from LLM output —
 * see AGENTS.md "LLM의 출력만으로 Action을 실행하지 않습니다".
 */
@Component
public class ActionExecutor {

	private static final Logger log = LoggerFactory.getLogger(ActionExecutor.class);

	private final ExecutionRepository executionRepository;
	private final ConversationRepository conversationRepository;
	private final PlatformConnectionRepository platformConnectionRepository;
	private final MessageRepository messageRepository;
	private final SlackApiClient slackApiClient;
	private final TokenEncryptor tokenEncryptor;
	private final Clock clock;

	public ActionExecutor(
			ExecutionRepository executionRepository,
			ConversationRepository conversationRepository,
			PlatformConnectionRepository platformConnectionRepository,
			MessageRepository messageRepository,
			SlackApiClient slackApiClient,
			TokenEncryptor tokenEncryptor,
			Clock clock
	) {
		this.executionRepository = executionRepository;
		this.conversationRepository = conversationRepository;
		this.platformConnectionRepository = platformConnectionRepository;
		this.messageRepository = messageRepository;
		this.slackApiClient = slackApiClient;
		this.tokenEncryptor = tokenEncryptor;
		this.clock = clock;
	}

	@Transactional
	public Execution execute(Baton baton, Long branchId, Long classificationId, Branch branch) {
		Execution execution = new Execution(baton.getId(), branchId, classificationId, branch.getActionType());
		execution = executionRepository.save(execution);

		try {
			Long resultMessageId = performAction(baton, branch);
			execution.succeed(resultMessageId, LocalDateTime.now(clock));
		} catch (Exception e) {
			// Never let an action-side failure (Slack error, bad token, anything unexpected) blow up the
			// caller's transaction — record it on the Execution instead so the pipeline degrades gracefully.
			log.error("Action execution failed for baton {}", baton.getId(), e);
			execution.fail(failureMessage(e), LocalDateTime.now(clock));
		}
		return execution;
	}

	@Transactional
	public Execution executeManualReply(Baton baton, String manualResponse) {
		Execution execution = new Execution(baton.getId(), null, null, ActionType.SEND_REPLY);
		execution = executionRepository.save(execution);

		try {
			Long resultMessageId = sendReplyText(baton, manualResponse);
			execution.succeed(resultMessageId, LocalDateTime.now(clock));
		} catch (Exception e) {
			log.error("Manual reply execution failed for baton {}", baton.getId(), e);
			execution.fail(failureMessage(e), LocalDateTime.now(clock));
		}
		return execution;
	}

	private String failureMessage(Exception e) {
		return e instanceof BusinessException ? e.getMessage() : "실행 중 예상하지 못한 오류가 발생했습니다.";
	}

	private Long performAction(Baton baton, Branch branch) {
		if (branch.getActionType() == ActionType.SEND_REPLY) {
			return sendReply(baton, branch);
		}
		// FORWARD / NOTIFY / REQUEST_HUMAN have no external side effect defined in the current
		// platform integration; they are recorded as successful internal signals only.
		return null;
	}

	private Long sendReply(Baton baton, Branch branch) {
		if (branch.getResponseText() == null || branch.getResponseText().isBlank()) {
			return null;
		}
		return sendReplyText(baton, branch.getResponseText());
	}

	private Long sendReplyText(Baton baton, String text) {
		Conversation conversation = conversationRepository.findById(baton.getConversationId())
				.orElseThrow(() -> new BusinessException(PlatformConnectionErrorCode.CONNECTION_NOT_FOUND));
		PlatformConnection connection = platformConnectionRepository.findById(conversation.getPlatformConnectionId())
				.orElseThrow(() -> new BusinessException(PlatformConnectionErrorCode.CONNECTION_NOT_FOUND));

		String accessToken = tokenEncryptor.decrypt(connection.getAccessTokenEncrypted());
		SlackChatPostMessageResponse response = slackApiClient.postMessage(
				accessToken, conversation.getExternalConversationId(), text);

		Message message = new Message(
				conversation.getId(),
				response.ts(),
				null,
				connection.getWorkspaceId(),
				SenderType.BATON,
				text,
				null,
				true,
				LocalDateTime.now(clock)
		);
		return messageRepository.save(message).getId();
	}
}
