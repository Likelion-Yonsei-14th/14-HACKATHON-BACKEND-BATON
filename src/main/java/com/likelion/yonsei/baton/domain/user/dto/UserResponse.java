package com.likelion.yonsei.baton.domain.user.dto;

import com.likelion.yonsei.baton.domain.user.entity.User;

import java.time.LocalDateTime;

public record UserResponse(
		Long id,
		String email,
		String name,
		String timezone,
		String language,
		LocalDateTime createdAt,
		LocalDateTime updatedAt
) {

	public static UserResponse from(User user) {
		return new UserResponse(
				user.getId(),
				user.getEmail(),
				user.getName(),
				user.getTimezone(),
				user.getLanguage(),
				user.getCreatedAt(),
				user.getUpdatedAt()
		);
	}
}
