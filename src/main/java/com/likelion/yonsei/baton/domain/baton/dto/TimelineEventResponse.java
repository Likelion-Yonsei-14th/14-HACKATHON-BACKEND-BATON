package com.likelion.yonsei.baton.domain.baton.dto;

import java.time.LocalDateTime;

public record TimelineEventResponse(
		String type,
		LocalDateTime occurredAt,
		String description
) {
}
