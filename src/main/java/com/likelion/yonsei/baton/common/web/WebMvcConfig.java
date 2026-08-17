package com.likelion.yonsei.baton.common.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

	private final CurrentUserIdArgumentResolver currentUserIdArgumentResolver;
	private final List<String> allowedOrigins;

	public WebMvcConfig(
			CurrentUserIdArgumentResolver currentUserIdArgumentResolver,
			@Value("${app.cors.allowed-origins:http://localhost:3000,http://localhost:5173}") String allowedOrigins
	) {
		this.currentUserIdArgumentResolver = currentUserIdArgumentResolver;
		this.allowedOrigins = Arrays.stream(allowedOrigins.split(","))
				.map(String::trim)
				.filter(origin -> !origin.isEmpty())
				.toList();
	}

	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
		resolvers.add(currentUserIdArgumentResolver);
	}

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		// APP_CORS_ALLOWED_ORIGINS existed as a documented env var since PR #3 but was never wired up,
		// so the browser silently blocked every cross-origin call the frontend made. Register it for real.
		registry.addMapping("/api/**")
				.allowedOrigins(allowedOrigins.toArray(new String[0]))
				.allowedMethods("GET", "POST", "PATCH", "DELETE", "OPTIONS")
				.allowedHeaders("Authorization", "Content-Type")
				.maxAge(3600);
	}
}
