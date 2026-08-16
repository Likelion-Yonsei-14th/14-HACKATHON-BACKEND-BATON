package com.likelion.yonsei.baton.domain.user.service;

import com.likelion.yonsei.baton.common.exception.BusinessException;
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

	public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Transactional
	public User signUp(UserSignUpRequest request) {
		if (userRepository.existsByEmail(request.email())) {
			throw new BusinessException(UserErrorCode.EMAIL_ALREADY_EXISTS);
		}
		User user = new User(
				request.email(),
				request.name(),
				passwordEncoder.encode(request.password()),
				request.timezone(),
				request.language()
		);
		return userRepository.save(user);
	}

	public User getById(Long userId) {
		return userRepository.findById(userId)
				.orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));
	}

	@Transactional
	public User update(Long userId, UserUpdateRequest request) {
		User user = getById(userId);
		user.update(request.name(), request.timezone(), request.language());
		return user;
	}

	@Transactional
	public void delete(Long userId) {
		User user = getById(userId);
		userRepository.delete(user);
	}
}
