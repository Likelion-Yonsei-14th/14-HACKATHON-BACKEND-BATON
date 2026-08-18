package com.likelion.yonsei.baton.domain.user.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
		@NotBlank String email,
		@NotBlank String password
) {
}
