package com.likelion.yonsei.baton.domain.baton.branch.entity;

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
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "branches")
public class Branch {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "baton_id", nullable = false)
	private Long batonId;

	@Column(nullable = false, length = 100)
	private String name;

	@Column(length = 500)
	private String description;

	@Column(name = "condition_text", nullable = false, columnDefinition = "TEXT")
	private String conditionText;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "condition_rule_json", columnDefinition = "jsonb")
	private String conditionRuleJson;

	@Column(name = "decision_text", nullable = false, columnDefinition = "TEXT")
	private String decisionText;

	@Column(name = "response_text", columnDefinition = "TEXT")
	private String responseText;

	@Enumerated(EnumType.STRING)
	@Column(name = "action_type", nullable = false, length = 30)
	private ActionType actionType;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "action_config_json", columnDefinition = "jsonb")
	private String actionConfigJson;

	@Enumerated(EnumType.STRING)
	@Column(name = "execution_mode", nullable = false, length = 30)
	private ExecutionMode executionMode;

	@Column(name = "sort_order", nullable = false)
	private int sortOrder;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	protected Branch() {
	}

	public Branch(
			Long batonId,
			String name,
			String description,
			String conditionText,
			String conditionRuleJson,
			String decisionText,
			String responseText,
			ActionType actionType,
			String actionConfigJson,
			ExecutionMode executionMode,
			int sortOrder
	) {
		this.batonId = batonId;
		this.name = name;
		this.description = description;
		this.conditionText = conditionText;
		this.conditionRuleJson = conditionRuleJson;
		this.decisionText = decisionText;
		this.responseText = responseText;
		this.actionType = actionType;
		this.actionConfigJson = actionConfigJson;
		this.executionMode = executionMode;
		this.sortOrder = sortOrder;
	}

	public void update(
			String name,
			String description,
			String conditionText,
			String conditionRuleJson,
			String decisionText,
			String responseText,
			ActionType actionType,
			String actionConfigJson,
			ExecutionMode executionMode,
			Integer sortOrder
	) {
		if (name != null) {
			this.name = name;
		}
		if (description != null) {
			this.description = description;
		}
		if (conditionText != null) {
			this.conditionText = conditionText;
		}
		if (conditionRuleJson != null) {
			this.conditionRuleJson = conditionRuleJson;
		}
		if (decisionText != null) {
			this.decisionText = decisionText;
		}
		if (responseText != null) {
			this.responseText = responseText;
		}
		if (actionType != null) {
			this.actionType = actionType;
		}
		if (actionConfigJson != null) {
			this.actionConfigJson = actionConfigJson;
		}
		if (executionMode != null) {
			this.executionMode = executionMode;
		}
		if (sortOrder != null) {
			this.sortOrder = sortOrder;
		}
	}

	public Long getId() {
		return id;
	}

	public Long getBatonId() {
		return batonId;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public String getConditionText() {
		return conditionText;
	}

	public String getConditionRuleJson() {
		return conditionRuleJson;
	}

	public String getDecisionText() {
		return decisionText;
	}

	public String getResponseText() {
		return responseText;
	}

	public ActionType getActionType() {
		return actionType;
	}

	public String getActionConfigJson() {
		return actionConfigJson;
	}

	public ExecutionMode getExecutionMode() {
		return executionMode;
	}

	public int getSortOrder() {
		return sortOrder;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}
}
