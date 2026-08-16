package com.likelion.yonsei.baton.domain.classification.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "classifications")
public class Classification {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "baton_id", nullable = false)
	private Long batonId;

	@Column(name = "reply_message_id", nullable = false)
	private Long replyMessageId;

	@Column(name = "selected_branch_id")
	private Long selectedBranchId;

	@Column(precision = 5, scale = 4)
	private BigDecimal confidence;

	@Column(name = "is_ambiguous", nullable = false)
	private boolean ambiguous;

	@Column(name = "contains_new_question", nullable = false)
	private boolean containsNewQuestion;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "extracted_data_json", columnDefinition = "jsonb")
	private String extractedDataJson;

	@Column(name = "reasoning_summary", columnDefinition = "TEXT")
	private String reasoningSummary;

	@Enumerated(EnumType.STRING)
	@Column(name = "result_status", nullable = false, length = 30)
	private ClassificationResultStatus resultStatus;

	@Column(name = "model_name", length = 100)
	private String modelName;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	protected Classification() {
	}

	public Classification(
			Long batonId,
			Long replyMessageId,
			Long selectedBranchId,
			BigDecimal confidence,
			boolean ambiguous,
			boolean containsNewQuestion,
			String extractedDataJson,
			String reasoningSummary,
			ClassificationResultStatus resultStatus,
			String modelName
	) {
		this.batonId = batonId;
		this.replyMessageId = replyMessageId;
		this.selectedBranchId = selectedBranchId;
		this.confidence = confidence;
		this.ambiguous = ambiguous;
		this.containsNewQuestion = containsNewQuestion;
		this.extractedDataJson = extractedDataJson;
		this.reasoningSummary = reasoningSummary;
		this.resultStatus = resultStatus;
		this.modelName = modelName;
	}

	public void markGuardrailRejected() {
		this.resultStatus = ClassificationResultStatus.GUARDRAIL_REJECTED;
	}

	public Long getId() {
		return id;
	}

	public Long getBatonId() {
		return batonId;
	}

	public Long getReplyMessageId() {
		return replyMessageId;
	}

	public Long getSelectedBranchId() {
		return selectedBranchId;
	}

	public BigDecimal getConfidence() {
		return confidence;
	}

	public boolean isAmbiguous() {
		return ambiguous;
	}

	public boolean isContainsNewQuestion() {
		return containsNewQuestion;
	}

	public String getExtractedDataJson() {
		return extractedDataJson;
	}

	public String getReasoningSummary() {
		return reasoningSummary;
	}

	public ClassificationResultStatus getResultStatus() {
		return resultStatus;
	}

	public String getModelName() {
		return modelName;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}
}
