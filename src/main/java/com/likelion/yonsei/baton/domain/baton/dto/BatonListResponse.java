package com.likelion.yonsei.baton.domain.baton.dto;

import java.util.List;

public record BatonListResponse(
		List<BatonSummaryResponse> batons,
		String nextCursor
) {
}
