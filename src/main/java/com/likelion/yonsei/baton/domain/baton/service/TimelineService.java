package com.likelion.yonsei.baton.domain.baton.service;

import com.likelion.yonsei.baton.domain.baton.dto.TimelineEventResponse;
import com.likelion.yonsei.baton.domain.baton.entity.Baton;
import com.likelion.yonsei.baton.domain.classification.entity.Classification;
import com.likelion.yonsei.baton.domain.classification.repository.ClassificationRepository;
import com.likelion.yonsei.baton.domain.execution.entity.Execution;
import com.likelion.yonsei.baton.domain.execution.repository.ExecutionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class TimelineService {

	private final BatonService batonService;
	private final ClassificationRepository classificationRepository;
	private final ExecutionRepository executionRepository;

	public TimelineService(BatonService batonService, ClassificationRepository classificationRepository, ExecutionRepository executionRepository) {
		this.batonService = batonService;
		this.classificationRepository = classificationRepository;
		this.executionRepository = executionRepository;
	}

	public List<TimelineEventResponse> build(Long batonId, Long userId) {
		Baton baton = batonService.getById(batonId, userId);
		List<TimelineEventResponse> events = new ArrayList<>();

		events.add(new TimelineEventResponse("CREATED", baton.getCreatedAt(), "BATON draft created"));
		if (baton.getActivatedAt() != null) {
			events.add(new TimelineEventResponse("ARMED", baton.getActivatedAt(), "BATON armed, waiting for reply"));
		}

		List<Classification> classifications = classificationRepository.findByBatonIdOrderByCreatedAtDesc(baton.getId());
		for (Classification classification : classifications) {
			events.add(new TimelineEventResponse("REPLY_RECEIVED", classification.getCreatedAt(), "Counterpart reply received"));
			events.add(new TimelineEventResponse(
					"CLASSIFICATION_" + classification.getResultStatus(),
					classification.getCreatedAt(),
					classification.getReasoningSummary() != null ? classification.getReasoningSummary() : "Classification: " + classification.getResultStatus()
			));
		}

		List<Execution> executions = executionRepository.findByBatonIdOrderByCreatedAtDesc(baton.getId());
		for (Execution execution : executions) {
			if (execution.getExecutedAt() == null) {
				continue;
			}
			String description = execution.getExecutionStatus() == com.likelion.yonsei.baton.domain.execution.entity.ExecutionStatus.SUCCESS
					? execution.getActionType() + " executed successfully"
					: execution.getActionType() + " failed: " + execution.getFailureReason();
			events.add(new TimelineEventResponse(
					execution.getActionType() + "_" + execution.getExecutionStatus(), execution.getExecutedAt(), description));
		}

		if (baton.getCompletedAt() != null) {
			events.add(new TimelineEventResponse("COMPLETED", baton.getCompletedAt(), "BATON " + baton.getStatus()));
		}

		events.sort(Comparator.comparing(TimelineEventResponse::occurredAt));
		return events;
	}
}
