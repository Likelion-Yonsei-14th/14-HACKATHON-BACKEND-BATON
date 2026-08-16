package com.likelion.yonsei.baton.domain.platform.entity;

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
@Table(name = "platform_connections")
public class PlatformConnection {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "user_id", nullable = false)
	private Long userId;

	@Enumerated(EnumType.STRING)
	@Column(name = "platform_type", nullable = false, length = 30)
	private PlatformType platformType;

	@Column(name = "workspace_id", nullable = false)
	private String workspaceId;

	@Column(name = "workspace_name")
	private String workspaceName;

	@Column(name = "access_token_encrypted", nullable = false, columnDefinition = "TEXT")
	private String accessTokenEncrypted;

	@Column(name = "refresh_token_encrypted", columnDefinition = "TEXT")
	private String refreshTokenEncrypted;

	@Column(name = "token_expires_at")
	private LocalDateTime tokenExpiresAt;

	@Enumerated(EnumType.STRING)
	@Column(name = "connection_status", nullable = false, length = 30)
	private ConnectionStatus connectionStatus;

	@Column(name = "last_synced_at")
	private LocalDateTime lastSyncedAt;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	protected PlatformConnection() {
	}

	public PlatformConnection(
			Long userId,
			PlatformType platformType,
			String workspaceId,
			String workspaceName,
			String accessTokenEncrypted,
			String refreshTokenEncrypted,
			LocalDateTime tokenExpiresAt
	) {
		this.userId = userId;
		this.platformType = platformType;
		this.workspaceId = workspaceId;
		this.workspaceName = workspaceName;
		this.accessTokenEncrypted = accessTokenEncrypted;
		this.refreshTokenEncrypted = refreshTokenEncrypted;
		this.tokenExpiresAt = tokenExpiresAt;
		this.connectionStatus = ConnectionStatus.CONNECTED;
	}

	public void markSynced(LocalDateTime syncedAt) {
		this.lastSyncedAt = syncedAt;
		this.connectionStatus = ConnectionStatus.CONNECTED;
	}

	public void markError() {
		this.connectionStatus = ConnectionStatus.ERROR;
	}

	public void disconnect() {
		this.connectionStatus = ConnectionStatus.DISCONNECTED;
	}

	public Long getId() {
		return id;
	}

	public Long getUserId() {
		return userId;
	}

	public PlatformType getPlatformType() {
		return platformType;
	}

	public String getWorkspaceId() {
		return workspaceId;
	}

	public String getWorkspaceName() {
		return workspaceName;
	}

	public String getAccessTokenEncrypted() {
		return accessTokenEncrypted;
	}

	public String getRefreshTokenEncrypted() {
		return refreshTokenEncrypted;
	}

	public LocalDateTime getTokenExpiresAt() {
		return tokenExpiresAt;
	}

	public ConnectionStatus getConnectionStatus() {
		return connectionStatus;
	}

	public LocalDateTime getLastSyncedAt() {
		return lastSyncedAt;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}
}
