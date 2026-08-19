package com.likelion.yonsei.baton.integration.localllm.dto;

import java.util.List;

public record LocalLlmChatResponse(
		String id,
		String model,
		List<Choice> choices
) {

	public record Choice(
			int index,
			LocalLlmChatMessage message,
			String finishReason
	) {
	}
}
