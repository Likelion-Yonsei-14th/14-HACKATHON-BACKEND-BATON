package com.likelion.yonsei.baton.domain.classification.controller;

import com.likelion.yonsei.baton.common.response.ApiResponse;
import com.likelion.yonsei.baton.common.web.CurrentUserId;
import com.likelion.yonsei.baton.domain.classification.dto.ClassificationListResponse;
import com.likelion.yonsei.baton.domain.classification.dto.ClassificationResponse;
import com.likelion.yonsei.baton.domain.classification.dto.ClassificationSummaryResponse;
import com.likelion.yonsei.baton.domain.classification.entity.Classification;
import com.likelion.yonsei.baton.domain.classification.service.ClassificationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ClassificationController {

	private final ClassificationService classificationService;
	private final ObjectMapper objectMapper;

	public ClassificationController(ClassificationService classificationService, ObjectMapper objectMapper) {
		this.classificationService = classificationService;
		this.objectMapper = objectMapper;
	}

	@GetMapping("/batons/{batonId}/classifications")
	public ApiResponse<ClassificationListResponse> list(@CurrentUserId Long userId, @PathVariable Long batonId) {
		List<Classification> classifications = classificationService.list(batonId, userId);
		return ApiResponse.success(new ClassificationListResponse(
				classifications.stream().map(ClassificationSummaryResponse::from).toList()));
	}

	@GetMapping("/classifications/{id}")
	public ApiResponse<ClassificationResponse> getById(@CurrentUserId Long userId, @PathVariable Long id) {
		Classification classification = classificationService.getById(id, userId);
		return ApiResponse.success(ClassificationResponse.from(classification, objectMapper));
	}
}
