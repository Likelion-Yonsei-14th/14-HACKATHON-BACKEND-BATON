package com.likelion.yonsei.baton.domain.baton.dto;

import java.util.List;

public record TimelineResponse(
		List<TimelineEventResponse> events
) {
}
