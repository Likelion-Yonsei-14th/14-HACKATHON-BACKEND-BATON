package com.likelion.yonsei.baton.domain.baton.branch.controller;

import com.likelion.yonsei.baton.common.response.ApiResponse;
import com.likelion.yonsei.baton.common.web.CurrentUserId;
import com.likelion.yonsei.baton.domain.baton.branch.dto.BranchCreateRequest;
import com.likelion.yonsei.baton.domain.baton.branch.dto.BranchCreateResponse;
import com.likelion.yonsei.baton.domain.baton.branch.dto.BranchDeleteResponse;
import com.likelion.yonsei.baton.domain.baton.branch.dto.BranchGenerateRequest;
import com.likelion.yonsei.baton.domain.baton.branch.dto.BranchGenerateResponse;
import com.likelion.yonsei.baton.domain.baton.branch.dto.BranchListResponse;
import com.likelion.yonsei.baton.domain.baton.branch.dto.BranchResponse;
import com.likelion.yonsei.baton.domain.baton.branch.dto.BranchSummaryResponse;
import com.likelion.yonsei.baton.domain.baton.branch.dto.BranchUpdateRequest;
import com.likelion.yonsei.baton.domain.baton.branch.dto.BranchUpdateResponse;
import com.likelion.yonsei.baton.domain.baton.branch.entity.Branch;
import com.likelion.yonsei.baton.domain.baton.branch.service.BranchGenerationService;
import com.likelion.yonsei.baton.domain.baton.branch.service.BranchService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@RestController
@RequestMapping("/api")
public class BranchController {

	private final BranchService branchService;
	private final BranchGenerationService branchGenerationService;
	private final ObjectMapper objectMapper;

	public BranchController(BranchService branchService, BranchGenerationService branchGenerationService, ObjectMapper objectMapper) {
		this.branchService = branchService;
		this.branchGenerationService = branchGenerationService;
		this.objectMapper = objectMapper;
	}

	@PostMapping("/batons/{batonId}/branches")
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponse<BranchCreateResponse> create(
			@CurrentUserId Long userId,
			@PathVariable Long batonId,
			@Valid @RequestBody BranchCreateRequest request
	) {
		Branch draft = new Branch(
				batonId,
				request.name(),
				request.description(),
				request.conditionText(),
				writeJson(request.conditionRuleJson()),
				request.decisionText(),
				request.responseText(),
				request.actionType(),
				writeJson(request.actionConfigJson()),
				request.executionMode(),
				request.sortOrder()
		);
		Branch branch = branchService.create(batonId, userId, draft);
		return ApiResponse.success(BranchCreateResponse.from(branch));
	}

	@PostMapping("/batons/{batonId}/branches/generate")
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponse<BranchGenerateResponse> generate(
			@CurrentUserId Long userId,
			@PathVariable Long batonId,
			@RequestBody(required = false) BranchGenerateRequest request
	) {
		String instruction = request != null ? request.additionalInstruction() : null;
		List<Branch> branches = branchGenerationService.generate(batonId, userId, instruction);
		return ApiResponse.success(new BranchGenerateResponse(branches.stream().map(BranchSummaryResponse::from).toList()));
	}

	@GetMapping("/batons/{batonId}/branches")
	public ApiResponse<BranchListResponse> list(@CurrentUserId Long userId, @PathVariable Long batonId) {
		List<Branch> branches = branchService.list(batonId, userId);
		return ApiResponse.success(new BranchListResponse(branches.stream().map(BranchSummaryResponse::from).toList()));
	}

	@GetMapping("/branches/{id}")
	public ApiResponse<BranchResponse> getById(@CurrentUserId Long userId, @PathVariable Long id) {
		Branch branch = branchService.getByIdForUser(id, userId);
		return ApiResponse.success(BranchResponse.from(branch, objectMapper));
	}

	@PatchMapping("/batons/{batonId}/branches/{id}")
	public ApiResponse<BranchUpdateResponse> update(
			@CurrentUserId Long userId,
			@PathVariable Long batonId,
			@PathVariable Long id,
			@RequestBody BranchUpdateRequest request
	) {
		Branch branch = branchService.update(
				batonId, id, userId,
				request.name(), request.description(), request.conditionText(), writeJson(request.conditionRuleJson()),
				request.decisionText(), request.responseText(), request.actionType(), writeJson(request.actionConfigJson()),
				request.executionMode(), request.sortOrder()
		);
		return ApiResponse.success(new BranchUpdateResponse(branch.getId(), branch.getUpdatedAt()));
	}

	@DeleteMapping("/batons/{batonId}/branches/{id}")
	public ApiResponse<BranchDeleteResponse> delete(@CurrentUserId Long userId, @PathVariable Long batonId, @PathVariable Long id) {
		branchService.delete(batonId, id, userId);
		return ApiResponse.success(new BranchDeleteResponse(id, true));
	}

	private String writeJson(Object value) {
		if (value == null) {
			return null;
		}
		try {
			return objectMapper.writeValueAsString(value);
		} catch (Exception e) {
			return null;
		}
	}
}
