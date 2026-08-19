package com.likelion.yonsei.baton.domain.message.controller;

import com.likelion.yonsei.baton.common.response.ApiResponse;
import com.likelion.yonsei.baton.common.web.CurrentUserId;
import com.likelion.yonsei.baton.domain.message.dto.MessageListResponse;
import com.likelion.yonsei.baton.domain.message.dto.MessageResponse;
import com.likelion.yonsei.baton.domain.message.dto.MessageSendRequest;
import com.likelion.yonsei.baton.domain.message.dto.MessageSyncRequest;
import com.likelion.yonsei.baton.domain.message.dto.MessageSyncResponse;
import com.likelion.yonsei.baton.domain.message.entity.Message;
import com.likelion.yonsei.baton.domain.message.service.MessageService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api")
public class MessageController {

	private final MessageService messageService;

	public MessageController(MessageService messageService) {
		this.messageService = messageService;
	}

	@GetMapping("/conversations/{conversationId}/messages")
	public ApiResponse<MessageListResponse> list(
			@CurrentUserId Long userId,
			@PathVariable Long conversationId,
			@RequestParam(required = false) Integer limit,
			@RequestParam(required = false) LocalDateTime before
	) {
		List<Message> messages = messageService.list(conversationId, userId, limit, before);
		return ApiResponse.success(new MessageListResponse(messages.stream().map(MessageResponse::from).toList()));
	}

	@GetMapping("/messages/{id}")
	public ApiResponse<MessageResponse> getById(@CurrentUserId Long userId, @PathVariable Long id) {
		Message message = messageService.getById(id, userId);
		return ApiResponse.success(MessageResponse.from(message));
	}

	@PostMapping("/conversations/{conversationId}/messages")
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponse<MessageResponse> send(
			@CurrentUserId Long userId,
			@PathVariable Long conversationId,
			@Valid @RequestBody MessageSendRequest request
	) {
		Message message = messageService.send(conversationId, userId, request.content());
		return ApiResponse.success(MessageResponse.from(message));
	}

	@PostMapping("/conversations/{conversationId}/messages/sync")
	public ApiResponse<MessageSyncResponse> sync(
			@CurrentUserId Long userId,
			@PathVariable Long conversationId,
			@RequestBody(required = false) MessageSyncRequest request
	) {
		Integer limit = request != null ? request.limit() : null;
		MessageService.MessageSyncResult result = messageService.sync(conversationId, userId, limit);
		return ApiResponse.success(new MessageSyncResponse(
				conversationId, result.syncedCount(), result.lastMessageId(), result.lastSyncedAt()));
	}
}
