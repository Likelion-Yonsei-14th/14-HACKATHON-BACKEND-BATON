package com.likelion.yonsei.baton.domain.user.dto;

import com.likelion.yonsei.baton.domain.user.entity.User;

import java.time.LocalDateTime;

/** apiKey is returned only here, once, at signup — it is never retrievable again. */
public record UserSignUpResponse(
		Long id,
		String email,
		String name,
		String timezone,
		String language,
		String apiKey,
		LocalDateTime createdAt
) {

	public static UserSignUpResponse from(User user, String apiKey) {
		return new UserSignUpResponse(
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
