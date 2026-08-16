package com.likelion.yonsei.baton.integration.openai.dto;

import java.util.List;

public record OpenAiChatResponse(
		String id,
		String model,
		List<Choice> choices
) {

	public record Choice(
			int index,
			OpenAiChatMessage message,
			String finishReason
	) {
	}
}
