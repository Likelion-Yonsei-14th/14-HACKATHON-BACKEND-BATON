package com.likelion.yonsei.baton.domain.conversation.controller;

import com.likelion.yonsei.baton.common.response.ApiResponse;
import com.likelion.yonsei.baton.common.web.CurrentUserId;
import com.likelion.yonsei.baton.domain.conversation.dto.ConversationListResponse;
import com.likelion.yonsei.baton.domain.conversation.dto.ConversationResponse;
import com.likelion.yonsei.baton.domain.conversation.dto.ConversationSummaryResponse;
import com.likelion.yonsei.baton.domain.conversation.entity.Conversation;
import com.likelion.yonsei.baton.domain.conversation.entity.ConversationType;
import com.likelion.yonsei.baton.domain.conversation.service.ConversationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/conversations")
public class ConversationController {

	private final ConversationService conversationService;

	public ConversationController(ConversationService conversationService) {
		this.conversationService = conversationService;
	}

	@GetMapping
	public ApiResponse<ConversationListResponse> list(
			@CurrentUserId Long userId,
			@RequestParam(required = false) Long platformConnectionId,
			@RequestParam(required = false) ConversationType type,
			@RequestParam(required = false) String cursor
	) {
		Long cursorId = parseCursor(cursor);
		List<Conversation> conversations = conversationService.search(userId, platformConnectionId, type, cursorId);
		List<ConversationSummaryResponse> items = conversations.stream().map(ConversationSummaryResponse::from).toList();
		String nextCursor = items.isEmpty() ? null : String.valueOf(conversations.get(conversations.size() - 1).getId());
		return ApiResponse.success(new ConversationListResponse(items, items.isEmpty() ? null : nextCursor));
	}

	@GetMapping("/{id}")
	public ApiResponse<ConversationResponse> getById(@CurrentUserId Long userId, @PathVariable Long id) {
		Conversation conversation = conversationService.getById(id, userId);
		return ApiResponse.success(ConversationResponse.from(conversation));
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
