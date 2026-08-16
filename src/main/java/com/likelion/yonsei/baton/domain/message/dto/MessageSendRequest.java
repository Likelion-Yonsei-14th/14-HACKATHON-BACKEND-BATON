package com.likelion.yonsei.baton.domain.message.dto;

import jakarta.validation.constraints.NotBlank;

public record MessageSendRequest(
		@NotBlank String content
) {
}
