package com.likelion.yonsei.baton.common.web;

import com.likelion.yonsei.baton.common.exception.BusinessException;
import com.likelion.yonsei.baton.common.exception.CommonErrorCode;
import com.likelion.yonsei.baton.domain.user.repository.UserRepository;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class CurrentUserIdArgumentResolver implements HandlerMethodArgumentResolver {

	private static final String HEADER = "X-User-Id";

	private final UserRepository userRepository;

	public CurrentUserIdArgumentResolver(UserRepository userRepository) {
		this.userRepository = userRepository;
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
		String header = webRequest.getHeader(HEADER);
		Long userId = parseOrNull(header);
		if (userId == null || !userRepository.existsById(userId)) {
			throw new BusinessException(CommonErrorCode.UNAUTHORIZED, "로그인이 필요합니다.");
		}
		return userId;
	}

	private Long parseOrNull(String header) {
		if (header == null || header.isBlank()) {
			return null;
		}
		try {
			return Long.parseLong(header.trim());
		} catch (NumberFormatException e) {
			return null;
		}
	}
}
