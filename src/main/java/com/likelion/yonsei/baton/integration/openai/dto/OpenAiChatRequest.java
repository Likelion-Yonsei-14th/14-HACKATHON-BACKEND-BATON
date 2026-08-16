package com.likelion.yonsei.baton.integration.openai.dto;

import java.util.List;
import java.util.Map;

public record OpenAiChatRequest(
		String model,
		List<OpenAiChatMessage> messages,
		Map<String, String> responseFormat
) {

	public static OpenAiChatRequest of(String model, List<OpenAiChatMessage> messages) {
		return new OpenAiChatRequest(model, messages, null);
	}

	public static OpenAiChatRequest ofJson(String model, List<OpenAiChatMessage> messages) {
		return new OpenAiChatRequest(model, messages, Map.of("type", "json_object"));
	}
}
