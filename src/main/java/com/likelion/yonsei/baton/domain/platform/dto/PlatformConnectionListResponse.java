package com.likelion.yonsei.baton.domain.platform.dto;

import java.util.List;

public record PlatformConnectionListResponse(
		List<PlatformConnectionSummaryResponse> connections
) {
}
