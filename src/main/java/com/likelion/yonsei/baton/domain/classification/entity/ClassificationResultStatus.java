package com.likelion.yonsei.baton.domain.classification.entity;

public enum ClassificationResultStatus {
	MATCHED,
	LOW_CONFIDENCE,
	NO_MATCH,
	AMBIGUOUS,
	GUARDRAIL_REJECTED
}
