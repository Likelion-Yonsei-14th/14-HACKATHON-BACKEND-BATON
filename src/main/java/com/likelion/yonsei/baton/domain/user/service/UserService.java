package com.likelion.yonsei.baton.domain.user.service;

import com.likelion.yonsei.baton.common.crypto.ApiKeyGenerator;
import com.likelion.yonsei.baton.common.exception.BusinessException;
import com.likelion.yonsei.baton.domain.user.dto.LoginRequest;
import com.likelion.yonsei.baton.domain.user.dto.UserSignUpRequest;
import com.likelion.yonsei.baton.domain.user.dto.UserUpdateRequest;
import com.likelion.yonsei.baton.domain.user.entity.User;
import com.likelion.yonsei.baton.domain.user.exception.UserErrorCode;
import com.likelion.yonsei.baton.domain.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final ApiKeyGenerator apiKeyGenerator;

	public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, ApiKeyGenerator apiKeyGenerator) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.apiKeyGenerator = apiKeyGenerator;
	}

	@Transactional
	public AuthResult signUp(UserSignUpRequest request) {
		if (userRepository.existsByEmail(request.email())) {
			throw new BusinessException(UserErrorCode.EMAIL_ALREADY_EXISTS);
		}
		String apiKey = apiKeyGenerator.generate();
		User user = new User(
				request.email(),
				request.name(),
				passwordEncoder.encode(request.password()),
				apiKeyGenerator.hash(apiKey),
				request.timezone(),
				request.language()
		);
		User saved = userRepository.save(user);
		return new AuthResult(saved, apiKey);
	}

	/**
	 * Issues a fresh api_key on every successful login and immediately invalidates the previous one
	 * (single active key per user — logging in elsewhere signs out any other session/device).
	 * Email-not-found and wrong-password both fail with the same INVALID_CREDENTIALS error so a caller
	 * can't use this endpoint to enumerate which emails are registered.
	 */
	@Transactional
	public AuthResult login(LoginRequest request) {
		User user = userRepository.findByEmail(request.email())
				.orElseThrow(() -> new BusinessException(UserErrorCode.INVALID_CREDENTIALS));
		if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
			throw new BusinessException(UserErrorCode.INVALID_CREDENTIALS);
		}

		String apiKey = apiKeyGenerator.generate();
		user.rotateApiKeyHash(apiKeyGenerator.hash(apiKey));
		return new AuthResult(user, apiKey);
	}

	/** Revokes the caller's current api_key without issuing a replacement, so it must be requested via login again. */
	@Transactional
	public void logout(Long userId) {
		User user = getById(userId);
		user.rotateApiKeyHash(apiKeyGenerator.hash(apiKeyGenerator.generate()));
	}

	public User getById(Long userId) {
		return userRepository.findById(userId)
				.orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));
	}

	@Transactional
	public User update(Long userId, UserUpdateRequest request) {
		User user = getById(userId);
		user.update(request.name(), request.timezone(), request.language(), request.llmProvider());
		return user;
	}

	@Transactional
	public void delete(Long userId) {
		User user = getById(userId);
		userRepository.delete(user);
	}

	/** apiKey is the one-time raw value; only its hash is ever persisted. */
	public record AuthResult(User user, String apiKey) {
	}
}
