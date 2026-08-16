package com.likelion.yonsei.baton.domain.execution.controller;

import com.likelion.yonsei.baton.common.response.ApiResponse;
import com.likelion.yonsei.baton.common.web.CurrentUserId;
import com.likelion.yonsei.baton.domain.execution.dto.BatonResolveRequest;
import com.likelion.yonsei.baton.domain.execution.dto.BatonResolveResponse;
import com.likelion.yonsei.baton.domain.execution.dto.ExecutionListResponse;
import com.likelion.yonsei.baton.domain.execution.dto.ExecutionResponse;
import com.likelion.yonsei.baton.domain.execution.dto.ExecutionSummaryResponse;
import com.likelion.yonsei.baton.domain.execution.entity.Execution;
import com.likelion.yonsei.baton.domain.execution.service.ExecutionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ExecutionController {

	private final ExecutionService executionService;

	public ExecutionController(ExecutionService executionService) {
		this.executionService = executionService;
	}

	@GetMapping("/batons/{batonId}/executions")
	public ApiResponse<ExecutionListResponse> list(@CurrentUserId Long userId, @PathVariable Long batonId) {
		List<Execution> executions = executionService.list(batonId, userId);
		return ApiResponse.success(new ExecutionListResponse(executions.stream().map(ExecutionSummaryResponse::from).toList()));
	}

	@GetMapping("/executions/{id}")
	public ApiResponse<ExecutionResponse> getById(@CurrentUserId Long userId, @PathVariable Long id) {
		Execution execution = executionService.getById(id, userId);
		return ApiResponse.success(ExecutionResponse.from(execution));
	}

	@PostMapping("/batons/{batonId}/resolve")
	public ApiResponse<BatonResolveResponse> resolve(
			@CurrentUserId Long userId,
			@PathVariable Long batonId,
			@Valid @RequestBody BatonResolveRequest request
	) {
		ExecutionService.ResolveResult result = executionService.resolve(batonId, userId, request);
		return ApiResponse.success(new BatonResolveResponse(batonId, result.status(), result.executionId(), result.resultMessageId()));
	}
}
