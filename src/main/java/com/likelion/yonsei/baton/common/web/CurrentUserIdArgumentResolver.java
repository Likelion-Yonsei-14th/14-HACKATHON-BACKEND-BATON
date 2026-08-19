package com.likelion.yonsei.baton.common.web;

import com.likelion.yonsei.baton.common.crypto.ApiKeyGenerator;
import com.likelion.yonsei.baton.common.exception.BusinessException;
import com.likelion.yonsei.baton.common.exception.CommonErrorCode;
import com.likelion.yonsei.baton.domain.user.entity.User;
import com.likelion.yonsei.baton.domain.user.repository.UserRepository;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * Resolves the authenticated user's id from an {@code Authorization: Bearer <api key>} header.
 * The raw key is never stored; it is hashed and compared against {@code users.api_key_hash}, so a
 * caller must know the secret issued at signup — not just guess a sequential user id.
 */
@Component
public class CurrentUserIdArgumentResolver implements HandlerMethodArgumentResolver {

	private static final String HEADER = "Authorization";
	private static final String BEARER_PREFIX = "Bearer ";

	private final UserRepository userRepository;
	private final ApiKeyGenerator apiKeyGenerator;

	public CurrentUserIdArgumentResolver(UserRepository userRepository, ApiKeyGenerator apiKeyGenerator) {
		this.userRepository = userRepository;
		this.apiKeyGenerator = apiKeyGenerator;
	}

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return parameter.hasParameterAnnotation(CurrentUserId.class) && Long.class.equals(parameter.getParameterType());
	}

	@Override
	public Object resolveArgument(
			MethodParameter parameter,
			ModelAndViewContainer mavContainer,
			NativeWebRequest webRequest,
			WebDataBinderFactory binderFactory
	) {
		String apiKey = extractApiKey(webRequest.getHeader(HEADER));
		if (apiKey == null) {
			throw new BusinessException(CommonErrorCode.UNAUTHORIZED, "로그인이 필요합니다.");
		}

		User user = userRepository.findByApiKeyHash(apiKeyGenerator.hash(apiKey))
				.orElseThrow(() -> new BusinessException(CommonErrorCode.UNAUTHORIZED, "로그인이 필요합니다."));
		return user.getId();
	}

	private String extractApiKey(String header) {
		if (header == null || !header.startsWith(BEARER_PREFIX)) {
			return null;
		}
		String apiKey = header.substring(BEARER_PREFIX.length()).trim();
		return apiKey.isEmpty() ? null : apiKey;
	}
}
