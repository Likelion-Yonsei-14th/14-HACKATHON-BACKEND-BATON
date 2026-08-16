package com.likelion.yonsei.baton.domain.user.dto;

public record UserUpdateRequest(
		String name,
		String timezone,
		String language
) {
}
