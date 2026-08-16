package com.likelion.yonsei.baton.domain.baton.controller;

import com.likelion.yonsei.baton.common.response.ApiResponse;
import com.likelion.yonsei.baton.common.web.CurrentUserId;
import com.likelion.yonsei.baton.domain.baton.dto.BatonActivateResponse;
import com.likelion.yonsei.baton.domain.baton.dto.BatonCancelResponse;
import com.likelion.yonsei.baton.domain.baton.dto.BatonCreateRequest;
import com.likelion.yonsei.baton.domain.baton.dto.BatonCreateResponse;
import com.likelion.yonsei.baton.domain.baton.dto.BatonDeleteResponse;
import com.likelion.yonsei.baton.domain.baton.dto.BatonListResponse;
import com.likelion.yonsei.baton.domain.baton.dto.BatonResponse;
import com.likelion.yonsei.baton.domain.baton.dto.BatonSummaryResponse;
import com.likelion.yonsei.baton.domain.baton.dto.BatonUpdateRequest;
import com.likelion.yonsei.baton.domain.baton.dto.BatonUpdateResponse;
import com.likelion.yonsei.baton.domain.baton.dto.TimelineResponse;
import com.likelion.yonsei.baton.domain.baton.entity.Baton;
import com.likelion.yonsei.baton.domain.baton.entity.BatonStatus;
import com.likelion.yonsei.baton.domain.baton.service.BatonService;
import com.likelion.yonsei.baton.domain.baton.service.TimelineService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/batons")
public class BatonController {

	private final BatonService batonService;
	private final TimelineService timelineService;

	public BatonController(BatonService batonService, TimelineService timelineService) {
		this.batonService = batonService;
		this.timelineService = timelineService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponse<BatonCreateResponse> create(@CurrentUserId Long userId, @Valid @RequestBody BatonCreateRequest request) {
		Baton baton = batonService.create(
				userId, request.conversationId(), request.triggerMessageId(), request.autoSendEnabled(), request.expiresAt());
		return ApiResponse.success(BatonCreateResponse.from(baton));
	}

	@GetMapping
	public ApiResponse<BatonListResponse> list(
			@CurrentUserId Long userId,
			@RequestParam(required = false) BatonStatus status,
			@RequestParam(required = false) Long conversationId,
			@RequestParam(required = false) String cursor
	) {
		Long cursorId = parseCursor(cursor);
		List<Baton> batons = batonService.search(userId, status, conversationId, cursorId);
		List<BatonSummaryResponse> items = batons.stream().map(BatonSummaryResponse::from).toList();
		String nextCursor = items.isEmpty() ? null : String.valueOf(batons.get(batons.size() - 1).getId());
		return ApiResponse.success(new BatonListResponse(items, nextCursor));
	}

	@GetMapping("/{id}")
	public ApiResponse<BatonResponse> getById(@CurrentUserId Long userId, @PathVariable Long id) {
		Baton baton = batonService.getById(id, userId);
		return ApiResponse.success(BatonResponse.from(baton));
	}

	@PatchMapping("/{id}")
	public ApiResponse<BatonUpdateResponse> update(
			@CurrentUserId Long userId,
			@PathVariable Long id,
			@RequestBody BatonUpdateRequest request
	) {
		Baton baton = batonService.update(id, userId, request.autoSendEnabled(), request.expiresAt());
		return ApiResponse.success(new BatonUpdateResponse(
				baton.getId(), baton.getStatus(), baton.isAutoSendEnabled(), baton.getExpiresAt(), baton.getUpdatedAt()));
	}

	@DeleteMapping("/{id}")
	public ApiResponse<BatonDeleteResponse> delete(@CurrentUserId Long userId, @PathVariable Long id) {
		batonService.delete(id, userId);
		return ApiResponse.success(new BatonDeleteResponse(id, true));
	}

	@PostMapping("/{id}/activate")
	public ApiResponse<BatonActivateResponse> activate(@CurrentUserId Long userId, @PathVariable Long id) {
		Baton baton = batonService.activate(id, userId);
		return ApiResponse.success(new BatonActivateResponse(baton.getId(), baton.getStatus(), baton.getActivatedAt()));
	}

	@PostMapping("/{id}/cancel")
	public ApiResponse<BatonCancelResponse> cancel(@CurrentUserId Long userId, @PathVariable Long id) {
		Baton baton = batonService.cancel(id, userId);
		return ApiResponse.success(new BatonCancelResponse(baton.getId(), baton.getStatus(), baton.getUpdatedAt()));
	}

	@GetMapping("/{id}/timeline")
	public ApiResponse<TimelineResponse> timeline(@CurrentUserId Long userId, @PathVariable Long id) {
		return ApiResponse.success(new TimelineResponse(timelineService.build(id, userId)));
	}

	private Long parseCursor(String cursor) {
		if (cursor == null || cursor.isBlank()) {
			return null;
		}
		try {
			return Long.parseLong(cursor);
		} catch (NumberFormatException e) {
			return null;
		}
	}
}
