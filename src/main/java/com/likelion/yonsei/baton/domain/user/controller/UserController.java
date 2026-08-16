package com.likelion.yonsei.baton.domain.user.controller;

import com.likelion.yonsei.baton.common.response.ApiResponse;
import com.likelion.yonsei.baton.common.web.CurrentUserId;
import com.likelion.yonsei.baton.domain.user.dto.UserDeleteResponse;
import com.likelion.yonsei.baton.domain.user.dto.UserResponse;
import com.likelion.yonsei.baton.domain.user.dto.UserSignUpRequest;
import com.likelion.yonsei.baton.domain.user.dto.UserUpdateRequest;
import com.likelion.yonsei.baton.domain.user.entity.User;
import com.likelion.yonsei.baton.domain.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Clock;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/users")
public class UserController {

	private final UserService userService;
	private final Clock clock;

	public UserController(UserService userService, Clock clock) {
		this.userService = userService;
		this.clock = clock;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponse<UserResponse> signUp(@Valid @RequestBody UserSignUpRequest request) {
		User user = userService.signUp(request);
		return ApiResponse.success(UserResponse.from(user));
	}

	@GetMapping("/me")
	public ApiResponse<UserResponse> getMe(@CurrentUserId Long userId) {
		User user = userService.getById(userId);
		return ApiResponse.success(UserResponse.from(user));
	}

	@PatchMapping("/me")
	public ApiResponse<UserResponse> updateMe(@CurrentUserId Long userId, @RequestBody UserUpdateRequest request) {
		User user = userService.update(userId, request);
		return ApiResponse.success(UserResponse.from(user));
	}

	@DeleteMapping("/me")
	public ApiResponse<UserDeleteResponse> deleteMe(@CurrentUserId Long userId) {
		userService.delete(userId);
		return ApiResponse.success(new UserDeleteResponse(true, LocalDateTime.now(clock)));
	}
}
