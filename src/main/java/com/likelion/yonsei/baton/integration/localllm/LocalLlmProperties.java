package com.likelion.yonsei.baton.integration.localllm;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Ollama serving Qwen3 0.6B locally — OpenAI-compatible /v1/chat/completions, no API key needed. */
@ConfigurationProperties(prefix = "ollama")
public record LocalLlmProperties(
		String baseUrl,
		String model
) {
}
