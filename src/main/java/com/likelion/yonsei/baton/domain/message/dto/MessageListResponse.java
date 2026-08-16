package com.likelion.yonsei.baton.domain.message.dto;

import java.util.List;

public record MessageListResponse(
		List<MessageResponse> messages
) {
}
