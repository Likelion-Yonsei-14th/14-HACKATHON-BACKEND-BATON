package com.likelion.yonsei.baton.domain.message.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
public class Message {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "conversation_id", nullable = false)
	private Long conversationId;

	@Column(name = "external_message_id", nullable = false)
	private String externalMessageId;

	@Column(name = "external_event_id")
	private String externalEventId;

	@Column(name = "sender_external_id", nullable = false)
	private String senderExternalId;

	@Enumerated(EnumType.STRING)
	@Column(name = "sender_type", nullable = false, length = 30)
	private SenderType senderType;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String content;

	@Column(name = "original_language")
	private String originalLanguage;

	@Column(name = "is_baton_generated", nullable = false)
	private boolean batonGenerated;

	@Column(name = "sent_at", nullable = false)
	private LocalDateTime sentAt;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	protected Message() {
	}

	public Message(
			Long conversationId,
			String externalMessageId,
			String externalEventId,
			String senderExternalId,
			SenderType senderType,
			String content,
			String originalLanguage,
			boolean batonGenerated,
			LocalDateTime sentAt
	) {
		this.conversationId = conversationId;
		this.externalMessageId = externalMessageId;
		this.externalEventId = externalEventId;
		this.senderExternalId = senderExternalId;
		this.senderType = senderType;
		this.content = content;
		this.originalLanguage = originalLanguage;
		this.batonGenerated = batonGenerated;
		this.sentAt = sentAt;
	}

	public Long getId() {
		return id;
	}

	public Long getConversationId() {
		return conversationId;
	}

	public String getExternalMessageId() {
		return externalMessageId;
	}

	public String getExternalEventId() {
		return externalEventId;
	}

	public String getSenderExternalId() {
		return senderExternalId;
	}

	public SenderType getSenderType() {
		return senderType;
	}

	public String getContent() {
		return content;
	}

	public String getOriginalLanguage() {
		return originalLanguage;
	}

	public boolean isBatonGenerated() {
		return batonGenerated;
	}

	public LocalDateTime getSentAt() {
		return sentAt;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}
}
