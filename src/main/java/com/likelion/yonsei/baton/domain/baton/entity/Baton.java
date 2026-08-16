package com.likelion.yonsei.baton.domain.baton.entity;

import com.likelion.yonsei.baton.common.exception.BusinessException;
import com.likelion.yonsei.baton.domain.baton.exception.BatonErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "batons")
public class Baton {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "user_id", nullable = false)
	private Long userId;

	@Column(name = "conversation_id", nullable = false)
	private Long conversationId;

	@Column(name = "trigger_message_id", nullable = false)
	private Long triggerMessageId;

	@Column(name = "reply_message_id")
	private Long replyMessageId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private BatonStatus status;

	@Column(name = "auto_send_enabled", nullable = false)
	private boolean autoSendEnabled;

	@Column(name = "expires_at")
	private LocalDateTime expiresAt;

	@Column(name = "activated_at")
	private LocalDateTime activatedAt;

	@Column(name = "completed_at")
	private LocalDateTime completedAt;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	protected Baton() {
	}

	public Baton(Long userId, Long conversationId, Long triggerMessageId, boolean autoSendEnabled, LocalDateTime expiresAt) {
		this.userId = userId;
		this.conversationId = conversationId;
		this.triggerMessageId = triggerMessageId;
		this.autoSendEnabled = autoSendEnabled;
		this.expiresAt = expiresAt;
		this.status = BatonStatus.DRAFT;
	}

	public void update(Boolean autoSendEnabled, LocalDateTime expiresAt) {
		requireMutable();
		if (autoSendEnabled != null) {
			this.autoSendEnabled = autoSendEnabled;
		}
		if (expiresAt != null) {
			this.expiresAt = expiresAt;
		}
	}

	public void activate(LocalDateTime now) {
		if (status != BatonStatus.DRAFT) {
			throw new BusinessException(BatonErrorCode.INVALID_STATUS_TRANSITION, "DRAFT 상태의 BATON만 활성화할 수 있습니다.");
		}
		this.status = BatonStatus.WAITING;
		this.activatedAt = now;
	}

	public void receiveReply(Long replyMessageId) {
		if (status != BatonStatus.WAITING) {
			throw new BusinessException(BatonErrorCode.INVALID_STATUS_TRANSITION, "WAITING 상태의 BATON만 답장을 받을 수 있습니다.");
		}
		this.replyMessageId = replyMessageId;
	}

	public void requireReview() {
		this.status = BatonStatus.PENDING_REVIEW;
	}

	public void markExecuted(LocalDateTime now) {
		this.status = BatonStatus.EXECUTED;
		this.completedAt = now;
	}

	public void complete(LocalDateTime now) {
		this.status = BatonStatus.COMPLETED;
		this.completedAt = now;
	}

	public void markError() {
		this.status = BatonStatus.ERROR;
	}

	public void cancel() {
		if (status != BatonStatus.WAITING && status != BatonStatus.PENDING_REVIEW) {
			throw new BusinessException(BatonErrorCode.INVALID_STATUS_TRANSITION, "WAITING 또는 PENDING_REVIEW 상태의 BATON만 취소할 수 있습니다.");
		}
		this.status = BatonStatus.CANCELLED;
	}

	public boolean isDeletable() {
		return status == BatonStatus.DRAFT || status == BatonStatus.CANCELLED || status == BatonStatus.EXPIRED;
	}

	public void expire() {
		this.status = BatonStatus.EXPIRED;
	}

	public boolean isTerminal() {
		return status == BatonStatus.COMPLETED
				|| status == BatonStatus.CANCELLED
				|| status == BatonStatus.EXPIRED
				|| status == BatonStatus.ERROR;
	}

	private void requireMutable() {
		if (status != BatonStatus.DRAFT) {
			throw new BusinessException(BatonErrorCode.INVALID_STATUS_TRANSITION, "DRAFT 상태의 BATON만 수정할 수 있습니다.");
		}
	}

	public Long getId() {
		return id;
	}

	public Long getUserId() {
		return userId;
	}

	public Long getConversationId() {
		return conversationId;
	}

	public Long getTriggerMessageId() {
		return triggerMessageId;
	}

	public Long getReplyMessageId() {
		return replyMessageId;
	}

	public BatonStatus getStatus() {
		return status;
	}

	public boolean isAutoSendEnabled() {
		return autoSendEnabled;
	}

	public LocalDateTime getExpiresAt() {
		return expiresAt;
	}

	public LocalDateTime getActivatedAt() {
		return activatedAt;
	}

	public LocalDateTime getCompletedAt() {
		return completedAt;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}
}
