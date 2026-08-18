package com.likelion.yonsei.baton.domain.user.controller;

import com.likelion.yonsei.baton.common.response.ApiResponse;
import com.likelion.yonsei.baton.common.web.CurrentUserId;
import com.likelion.yonsei.baton.domain.user.dto.LoginRequest;
import com.likelion.yonsei.baton.domain.user.dto.UserAuthResponse;
import com.likelion.yonsei.baton.domain.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AuthController {

	private final UserService userService;

	public AuthController(UserService userService) {
		this.userService = userService;
	}

	@PostMapping("/login")
	public ApiResponse<UserAuthResponse> login(@Valid @RequestBody LoginRequest request) {
		UserService.AuthResult result = userService.login(request);
		return ApiResponse.success(UserAuthResponse.from(result.user(), result.apiKey()));
	}

	@PostMapping("/logout")
	public ApiResponse<Void> logout(@CurrentUserId Long userId) {
		userService.logout(userId);
		return ApiResponse.successEmpty();
	}
}
