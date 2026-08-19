package com.likelion.yonsei.baton.integration.localllm;

import com.likelion.yonsei.baton.common.exception.BusinessException;
import com.likelion.yonsei.baton.integration.llm.ChatCompletionClient;
import com.likelion.yonsei.baton.integration.localllm.dto.LocalLlmChatMessage;
import com.likelion.yonsei.baton.integration.localllm.dto.LocalLlmChatRequest;
import com.likelion.yonsei.baton.integration.localllm.dto.LocalLlmChatResponse;
import com.likelion.yonsei.baton.integration.localllm.exception.LocalLlmErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.SocketTimeoutException;
import java.util.List;

/** Talks to the Ollama instance running Qwen3 0.6B in-cluster — same OpenAI-shaped wire format, no API key. */
@Component
public class LocalLlmClient implements ChatCompletionClient {

	private static final Logger log = LoggerFactory.getLogger(LocalLlmClient.class);

	private final RestClient ollamaRestClient;
	private final LocalLlmProperties properties;

	public LocalLlmClient(RestClient ollamaRestClient, LocalLlmProperties properties) {
		this.ollamaRestClient = ollamaRestClient;
		this.properties = properties;
	}

	@Override
	public String chat(String prompt) {
		LocalLlmChatRequest request = LocalLlmChatRequest.of(properties.model(), List.of(LocalLlmChatMessage.user(prompt)));
		return extractContent(callChatCompletions(request));
	}

	@Override
	public String chatJson(String systemPrompt, String userPrompt) {
		LocalLlmChatRequest request = LocalLlmChatRequest.ofJson(
				properties.model(),
				List.of(LocalLlmChatMessage.system(systemPrompt), LocalLlmChatMessage.user(userPrompt))
		);
		return extractContent(callChatCompletions(request));
	}

	private String extractContent(LocalLlmChatResponse response) {
		if (response == null || response.choices() == null || response.choices().isEmpty()) {
			log.error("Local LLM returned an empty response body");
			throw new BusinessException(LocalLlmErrorCode.EMPTY_RESPONSE);
		}
		return response.choices().get(0).message().content();
	}

	private LocalLlmChatResponse callChatCompletions(LocalLlmChatRequest request) {
		try {
			return ollamaRestClient.post()
					.uri("/chat/completions")
					.body(request)
					.retrieve()
					.body(LocalLlmChatResponse.class);
		} catch (HttpServerErrorException e) {
			log.error("Local LLM server error: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
			throw new BusinessException(LocalLlmErrorCode.UPSTREAM_ERROR);
		} catch (ResourceAccessException e) {
			if (e.getCause() instanceof SocketTimeoutException) {
				log.error("Local LLM request timed out", e);
				throw new BusinessException(LocalLlmErrorCode.TIMEOUT);
			}
			log.error("Local LLM request could not reach the server", e);
			throw new BusinessException(LocalLlmErrorCode.REQUEST_FAILED);
		} catch (RestClientException e) {
			log.error("Local LLM request failed", e);
			throw new BusinessException(LocalLlmErrorCode.REQUEST_FAILED);
		}
	}
}
