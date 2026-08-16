package com.likelion.yonsei.baton.integration.openai.dto;

import java.util.List;

public record OpenAiChatRequest(
		String model,
		List<OpenAiChatMessage> messages
) {
}
