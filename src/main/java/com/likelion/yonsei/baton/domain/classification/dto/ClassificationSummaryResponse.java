package com.likelion.yonsei.baton.domain.classification.dto;

import com.likelion.yonsei.baton.domain.classification.entity.Classification;
import com.likelion.yonsei.baton.domain.classification.entity.ClassificationResultStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ClassificationSummaryResponse(
		Long id,
		Long selectedBranchId,
		BigDecimal confidence,
		boolean isAmbiguous,
		boolean containsNewQuestion,
		String reasoningSummary,
		ClassificationResultStatus resultStatus,
		LocalDateTime createdAt
) {

	public static ClassificationSummaryResponse from(Classification classification) {
		return new ClassificationSummaryResponse(
				classification.getId(),
				classification.getSelectedBranchId(),
				classification.getConfidence(),
				classification.isAmbiguous(),
				classification.isContainsNewQuestion(),
				classification.getReasoningSummary(),
				classification.getResultStatus(),
				classification.getCreatedAt()
		);
	}
}
