package com.likelion.yonsei.baton.integration.localllm.dto;

import java.util.List;
import java.util.Map;

public record LocalLlmChatRequest(
		String model,
		List<LocalLlmChatMessage> messages,
		Map<String, String> responseFormat,
		boolean stream
) {

	public static LocalLlmChatRequest of(String model, List<LocalLlmChatMessage> messages) {
		return new LocalLlmChatRequest(model, messages, null, false);
	}

	public static LocalLlmChatRequest ofJson(String model, List<LocalLlmChatMessage> messages) {
		return new LocalLlmChatRequest(model, messages, Map.of("type", "json_object"), false);
	}
}
