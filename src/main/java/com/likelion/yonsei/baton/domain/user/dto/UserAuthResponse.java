package com.likelion.yonsei.baton.domain.user.dto;

import com.likelion.yonsei.baton.domain.user.entity.User;

import java.time.LocalDateTime;

/** apiKey is returned only here, once, at signup or login — it is never retrievable again afterward. */
public record UserAuthResponse(
		Long id,
		String email,
		String name,
		String timezone,
		String language,
		String apiKey,
		LocalDateTime createdAt
) {

	public static UserAuthResponse from(User user, String apiKey) {
		return new UserAuthResponse(
				user.getId(),
				user.getEmail(),
				user.getName(),
				user.getTimezone(),
				user.getLanguage(),
				apiKey,
				user.getCreatedAt()
		);
	}
}
