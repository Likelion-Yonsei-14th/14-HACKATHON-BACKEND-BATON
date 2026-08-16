package com.likelion.yonsei.baton.domain.classification.dto;

import java.math.BigDecimal;
import java.util.Map;

/** Parsed shape of the OpenAI structured-output response for reply classification. */
public record AiClassificationResult(
		Long selectedBranchId,
		BigDecimal confidence,
		boolean ambiguous,
		boolean containsNewQuestion,
		Map<String, Object> extractedData,
		String reasoningSummary
) {
}
