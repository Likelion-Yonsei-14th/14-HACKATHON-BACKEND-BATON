package com.likelion.yonsei.baton.integration.openai.dto;

public record OpenAiChatMessage(
		String role,
		String content
) {

	public static OpenAiChatMessage user(String content) {
		return new OpenAiChatMessage("user", content);
	}

	public static OpenAiChatMessage system(String content) {
		return new OpenAiChatMessage("system", content);
	}
}
