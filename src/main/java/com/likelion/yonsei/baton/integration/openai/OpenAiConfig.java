package com.likelion.yonsei.baton.integration.openai;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
@EnableConfigurationProperties(OpenAiProperties.class)
public class OpenAiConfig {

	private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
	private static final Duration READ_TIMEOUT = Duration.ofSeconds(30);

	@Bean
	public RestClient openAiRestClient(OpenAiProperties properties) {
		SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
		requestFactory.setConnectTimeout(CONNECT_TIMEOUT);
		requestFactory.setReadTimeout(READ_TIMEOUT);

		return RestClient.builder()
				.baseUrl(properties.baseUrl())
				.requestFactory(requestFactory)
				.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + properties.apiKey())
				.defaultHeader(HttpHeaders.CONTENT_TYPE, "application/json")
				.build();
	}
}
