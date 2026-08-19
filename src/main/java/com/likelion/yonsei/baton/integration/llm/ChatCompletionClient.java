package com.likelion.yonsei.baton.integration.llm;

/** Common shape both the local Qwen3 (Ollama) and OpenAI clients implement, so callers don't care which one they got. */
public interface ChatCompletionClient {

	String chat(String prompt);

	/** Structured-output call: instructs the model to return a single JSON object matching the caller's documented schema. */
	String chatJson(String systemPrompt, String userPrompt);
}
