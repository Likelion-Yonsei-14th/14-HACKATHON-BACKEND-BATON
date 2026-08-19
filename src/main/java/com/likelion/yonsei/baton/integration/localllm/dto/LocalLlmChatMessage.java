package com.likelion.yonsei.baton.integration.localllm.dto;

public record LocalLlmChatMessage(
		String role,
		String content
) {

	public static LocalLlmChatMessage user(String content) {
		return new LocalLlmChatMessage("user", content);
	}

	public static LocalLlmChatMessage system(String content) {
		return new LocalLlmChatMessage("system", content);
	}
}
