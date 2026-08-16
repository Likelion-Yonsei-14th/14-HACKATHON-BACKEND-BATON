package com.likelion.yonsei.baton.domain.execution.entity;

import com.likelion.yonsei.baton.domain.baton.branch.entity.ActionType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "executions")
public class Execution {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "baton_id", nullable = false)
	private Long batonId;

	@Column(name = "branch_id")
	private Long branchId;

	@Column(name = "classification_id")
	private Long classificationId;

	@Enumerated(EnumType.STRING)
	@Column(name = "action_type", nullable = false, length = 30)
	private ActionType actionType;

	@Enumerated(EnumType.STRING)
	@Column(name = "execution_status", nullable = false, length = 30)
	private ExecutionStatus executionStatus;

	@Column(name = "result_message_id")
	private Long resultMessageId;

	@Column(name = "executed_at")
	private LocalDateTime executedAt;

	@Column(name = "failure_reason", columnDefinition = "TEXT")
	private String failureReason;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	protected Execution() {
	}

	public Execution(Long batonId, Long branchId, Long classificationId, ActionType actionType) {
		this.batonId = batonId;
		this.branchId = branchId;
		this.classificationId = classificationId;
		this.actionType = actionType;
		this.executionStatus = ExecutionStatus.PENDING;
	}

	public void succeed(Long resultMessageId, LocalDateTime executedAt) {
		this.executionStatus = ExecutionStatus.SUCCESS;
		this.resultMessageId = resultMessageId;
		this.executedAt = executedAt;
	}

	public void fail(String failureReason, LocalDateTime executedAt) {
		this.executionStatus = ExecutionStatus.FAILED;
		this.failureReason = failureReason;
		this.executedAt = executedAt;
	}

	public void cancel(String reason) {
		this.executionStatus = ExecutionStatus.CANCELLED;
		this.failureReason = reason;
	}

	public Long getId() {
		return id;
	}

	public Long getBatonId() {
		return batonId;
	}

	public Long getBranchId() {
		return branchId;
	}

	public Long getClassificationId() {
		return classificationId;
	}

	public ActionType getActionType() {
		return actionType;
	}

	public ExecutionStatus getExecutionStatus() {
		return executionStatus;
	}

	public Long getResultMessageId() {
		return resultMessageId;
	}

	public LocalDateTime getExecutedAt() {
		return executedAt;
	}

	public String getFailureReason() {
		return failureReason;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}
}
