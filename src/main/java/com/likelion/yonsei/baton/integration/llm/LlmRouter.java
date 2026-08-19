package com.likelion.yonsei.baton.integration.llm;

import com.likelion.yonsei.baton.domain.user.entity.LlmProvider;
import com.likelion.yonsei.baton.domain.user.entity.User;
import com.likelion.yonsei.baton.domain.user.service.UserService;
import com.likelion.yonsei.baton.integration.localllm.LocalLlmClient;
import com.likelion.yonsei.baton.integration.localllm.LocalLlmProperties;
import com.likelion.yonsei.baton.integration.openai.OpenAiClient;
import com.likelion.yonsei.baton.integration.openai.OpenAiProperties;
import org.springframework.stereotype.Component;

/**
 * Picks OpenAI vs. the local Qwen3 model per user. Defaults to local (LlmProvider.LOCAL) so a fresh
 * account works without anyone configuring an OpenAI key — OpenAI is opt-in from 개인설정.
 */
@Component
public class LlmRouter {

	private final UserService userService;
	private final LocalLlmClient localLlmClient;
	private final OpenAiClient openAiClient;
	private final LocalLlmProperties localLlmProperties;
	private final OpenAiProperties openAiProperties;

	public LlmRouter(
			UserService userService,
			LocalLlmClient localLlmClient,
			OpenAiClient openAiClient,
			LocalLlmProperties localLlmProperties,
			OpenAiProperties openAiProperties
	) {
		this.userService = userService;
		this.localLlmClient = localLlmClient;
		this.openAiClient = openAiClient;
		this.localLlmProperties = localLlmProperties;
		this.openAiProperties = openAiProperties;
	}

	public ChatCompletionClient forUser(Long userId) {
		return isOpenAi(userId) ? openAiClient : localLlmClient;
	}

	/** Callers use this to pick a simpler prompt variant for the local model — it needs a lot more hand-holding than OpenAI. */
	public boolean isLocal(Long userId) {
		return !isOpenAi(userId);
	}

	/** Name recorded on Classification/Branch rows for debugging — reflects whichever provider actually ran. */
	public String modelNameForUser(Long userId) {
		return isOpenAi(userId) ? openAiProperties.model() : localLlmProperties.model();
	}

	private boolean isOpenAi(Long userId) {
		User user = userService.getById(userId);
		return user.getLlmProvider() == LlmProvider.OPENAI;
	}
}
