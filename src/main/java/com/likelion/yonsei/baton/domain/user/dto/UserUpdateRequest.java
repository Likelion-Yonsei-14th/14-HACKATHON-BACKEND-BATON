package com.likelion.yonsei.baton.domain.user.dto;

import com.likelion.yonsei.baton.domain.user.entity.LlmProvider;

public record UserUpdateRequest(
		String name,
		String timezone,
		String language,
		LlmProvider llmProvider
) {
}
