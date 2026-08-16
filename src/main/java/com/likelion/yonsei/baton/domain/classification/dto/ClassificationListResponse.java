package com.likelion.yonsei.baton.domain.classification.dto;

import java.util.List;

public record ClassificationListResponse(
		List<ClassificationSummaryResponse> classifications
) {
}
