package com.likelion.yonsei.baton.domain.classification.dto;

import com.likelion.yonsei.baton.domain.classification.entity.Classification;
import com.likelion.yonsei.baton.domain.classification.entity.ClassificationResultStatus;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ClassificationResponse(
		Long id,
		Long batonId,
		Long selectedBranchId,
		BigDecimal confidence,
		boolean isAmbiguous,
		boolean containsNewQuestion,
		JsonNode extractedDataJson,
		String reasoningSummary,
		ClassificationResultStatus resultStatus,
		String modelName,
		LocalDateTime createdAt
) {

	public static ClassificationResponse from(Classification classification, ObjectMapper objectMapper) {
		JsonNode extracted = null;
		if (classification.getExtractedDataJson() != null) {
			try {
				extracted = objectMapper.readTree(classification.getExtractedDataJson());
			} catch (Exception ignored) {
				// leave null if malformed
			}
		}
		return new ClassificationResponse(
				classification.getId(),
				classification.getBatonId(),
				classification.getSelectedBranchId(),
				classification.getConfidence(),
				classification.isAmbiguous(),
				classification.isContainsNewQuestion(),
				extracted,
				classification.getReasoningSummary(),
				classification.getResultStatus(),
				classification.getModelName(),
				classification.getCreatedAt()
		);
	}
}
