package com.likelion.yonsei.baton.domain.baton.dto;

import com.likelion.yonsei.baton.domain.baton.entity.BatonStatus;

import java.time.LocalDateTime;

public record BatonActivateResponse(
		Long id,
		BatonStatus status,
		LocalDateTime activatedAt
) {
}
