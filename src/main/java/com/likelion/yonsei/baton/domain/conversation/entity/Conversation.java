package com.likelion.yonsei.baton.domain.conversation.entity;

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
@Table(name = "conversations")
public class Conversation {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "platform_connection_id", nullable = false)
	private Long platformConnectionId;

	@Column(name = "external_conversation_id", nullable = false)
	private String externalConversationId;

	@Column(name = "external_thread_id")
	private String externalThreadId;

	@Enumerated(EnumType.STRING)
	@Column(name = "conversation_type", nullable = false, length = 30)
	private ConversationType conversationType;

	private String title;

	@Column(name = "counterpart_external_id")
	private String counterpartExternalId;

	@Column(name = "counterpart_name")
	private String counterpartName;

	@Column(name = "counterpart_timezone")
	private String counterpartTimezone;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	protected Conversation() {
	}

	public Conversation(
			Long platformConnectionId,
			String externalConversationId,
			String externalThreadId,
			ConversationType conversationType,
			String title,
			String counterpartExternalId,
			String counterpartName,
			String counterpartTimezone
	) {
		this.platformConnectionId = platformConnectionId;
		this.externalConversationId = externalConversationId;
		this.externalThreadId = externalThreadId;
		this.conversationType = conversationType;
		this.title = title;
		this.counterpartExternalId = counterpartExternalId;
		this.counterpartName = counterpartName;
		this.counterpartTimezone = counterpartTimezone;
	}

	public Long getId() {
		return id;
	}

	public Long getPlatformConnectionId() {
		return platformConnectionId;
	}

	public String getExternalConversationId() {
		return externalConversationId;
	}

	public String getExternalThreadId() {
		return externalThreadId;
	}

	public ConversationType getConversationType() {
		return conversationType;
	}

	public String getTitle() {
		return title;
	}

	public String getCounterpartExternalId() {
		return counterpartExternalId;
	}

	public String getCounterpartName() {
		return counterpartName;
	}

	public String getCounterpartTimezone() {
		return counterpartTimezone;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}
}
