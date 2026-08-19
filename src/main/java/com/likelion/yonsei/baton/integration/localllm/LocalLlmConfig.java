package com.likelion.yonsei.baton.integration.localllm;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
@EnableConfigurationProperties(LocalLlmProperties.class)
public class LocalLlmConfig {

	private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
	// CPU inference on a 2vCore box is slow — give it real room before we call it a timeout.
	private static final Duration READ_TIMEOUT = Duration.ofSeconds(90);

	@Bean
	public RestClient ollamaRestClient(RestClient.Builder restClientBuilder, LocalLlmProperties properties) {
		SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
		requestFactory.setConnectTimeout(CONNECT_TIMEOUT);
		requestFactory.setReadTimeout(READ_TIMEOUT);

		return restClientBuilder
				.baseUrl(properties.baseUrl())
				.requestFactory(requestFactory)
				.defaultHeader(HttpHeaders.CONTENT_TYPE, "application/json")
				.build();
	}
}
