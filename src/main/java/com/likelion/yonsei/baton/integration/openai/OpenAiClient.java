package com.likelion.yonsei.baton.integration.openai;

import com.likelion.yonsei.baton.common.exception.BusinessException;
import com.likelion.yonsei.baton.integration.openai.dto.OpenAiChatMessage;
import com.likelion.yonsei.baton.integration.openai.dto.OpenAiChatRequest;
import com.likelion.yonsei.baton.integration.openai.dto.OpenAiChatResponse;
import com.likelion.yonsei.baton.integration.openai.exception.OpenAiErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.SocketTimeoutException;
import java.util.List;

@Component
public class OpenAiClient {

	private static final Logger log = LoggerFactory.getLogger(OpenAiClient.class);

	private final RestClient openAiRestClient;
	private final OpenAiProperties properties;

	public OpenAiClient(RestClient openAiRestClient, OpenAiProperties properties) {
		this.openAiRestClient = openAiRestClient;
		this.properties = properties;
	}

	public String chat(String prompt) {
		requireConfigured();
		OpenAiChatRequest request = OpenAiChatRequest.of(properties.model(), List.of(OpenAiChatMessage.user(prompt)));
		return extractContent(callChatCompletions(request));
	}

	/** Structured-output call: instructs the model to return a single JSON object matching the caller's documented schema. */
	public String chatJson(String systemPrompt, String userPrompt) {
		requireConfigured();
		OpenAiChatRequest request = OpenAiChatRequest.ofJson(
				properties.model(),
				List.of(OpenAiChatMessage.system(systemPrompt), OpenAiChatMessage.user(userPrompt))
		);
		return extractContent(callChatCompletions(request));
	}

	private void requireConfigured() {
		if (properties.apiKey() == null || properties.apiKey().isBlank()) {
			log.error("OpenAI API key is not configured");
			throw new BusinessException(OpenAiErrorCode.MISCONFIGURED);
		}
	}

	private String extractContent(OpenAiChatResponse response) {
		if (response == null || response.choices() == null || response.choices().isEmpty()) {
			log.error("OpenAI returned an empty response body");
			throw new BusinessException(OpenAiErrorCode.EMPTY_RESPONSE);
		}
		return response.choices().get(0).message().content();
	}

	private OpenAiChatResponse callChatCompletions(OpenAiChatRequest request) {
		try {
			return openAiRestClient.post()
					.uri("/chat/completions")
					.body(request)
					.retrieve()
					.body(OpenAiChatResponse.class);
		} catch (HttpClientErrorException e) {
			throw mapClientError(e);
		} catch (HttpServerErrorException e) {
			log.error("OpenAI server error: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
			throw new BusinessException(OpenAiErrorCode.UPSTREAM_ERROR);
		} catch (ResourceAccessException e) {
			if (e.getCause() instanceof SocketTimeoutException) {
				log.error("OpenAI request timed out", e);
				throw new BusinessException(OpenAiErrorCode.TIMEOUT);
			}
			log.error("OpenAI request could not reach the server", e);
			throw new BusinessException(OpenAiErrorCode.REQUEST_FAILED);
		} catch (RestClientException e) {
			log.error("OpenAI request failed", e);
			throw new BusinessException(OpenAiErrorCode.REQUEST_FAILED);
		}
	}

	private BusinessException mapClientError(HttpClientErrorException e) {
		HttpStatusCode status = e.getStatusCode();
		log.error("OpenAI client error: status={}, body={}", status, e.getResponseBodyAsString());

		if (status.value() == 401 || status.value() == 403) {
			return new BusinessException(OpenAiErrorCode.UNAUTHORIZED);
		}
		if (status.value() == 429) {
			return new BusinessException(OpenAiErrorCode.RATE_LIMITED);
		}
		return new BusinessException(OpenAiErrorCode.INVALID_REQUEST);
	}
}
