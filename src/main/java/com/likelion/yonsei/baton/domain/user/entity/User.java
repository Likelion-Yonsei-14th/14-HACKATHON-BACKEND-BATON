package com.likelion.yonsei.baton.domain.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true)
	private String email;

	@Column(nullable = false)
	private String name;

	@Column(name = "password_hash", nullable = false)
	private String passwordHash;

	@Column(name = "api_key_hash", nullable = false, unique = true, length = 64)
	private String apiKeyHash;

	private String timezone;

	private String language;

	@CreationTimestamp
	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@UpdateTimestamp
	@Column(nullable = false)
	private LocalDateTime updatedAt;

	protected User() {
	}

	public User(String email, String name, String passwordHash, String apiKeyHash, String timezone, String language) {
		this.email = email;
		this.name = name;
		this.passwordHash = passwordHash;
		this.apiKeyHash = apiKeyHash;
		this.timezone = timezone;
		this.language = language;
	}

	public void update(String name, String timezone, String language) {
		if (name != null) {
			this.name = name;
		}
		if (timezone != null) {
			this.timezone = timezone;
		}
		if (language != null) {
			this.language = language;
		}
	}

	public Long getId() {
		return id;
	}

	public String getEmail() {
		return email;
	}

	public String getName() {
		return name;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public String getApiKeyHash() {
		return apiKeyHash;
	}

	public String getTimezone() {
		return timezone;
	}

	public String getLanguage() {
		return language;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}
}
