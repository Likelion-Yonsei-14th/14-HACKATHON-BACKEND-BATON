package com.likelion.yonsei.baton.domain.user.dto;

import java.time.LocalDateTime;

public record UserDeleteResponse(
		boolean deleted,
		LocalDateTime deletedAt
) {
}
