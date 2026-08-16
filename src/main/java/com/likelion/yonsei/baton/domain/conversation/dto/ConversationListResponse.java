package com.likelion.yonsei.baton.domain.conversation.dto;

import java.util.List;

public record ConversationListResponse(
		List<ConversationSummaryResponse> conversations,
		String nextCursor
) {
}
