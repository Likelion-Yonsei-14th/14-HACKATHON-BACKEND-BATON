package com.likelion.yonsei.baton.domain.platform.exception;

import com.likelion.yonsei.baton.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum PlatformConnectionErrorCode implements ErrorCode {

	CONNECTION_NOT_FOUND(HttpStatus.NOT_FOUND, "PLATFORM-001", "플랫폼 연결을 찾을 수 없습니다."),
	SLACK_OAUTH_FAILED(HttpStatus.BAD_GATEWAY, "PLATFORM-002", "Slack 인증에 실패했습니다."),
	SLACK_API_FAILED(HttpStatus.BAD_GATEWAY, "PLATFORM-003", "Slack API 요청 중 오류가 발생했습니다."),
	INVALID_OAUTH_STATE(HttpStatus.BAD_REQUEST, "PLATFORM-004", "유효하지 않은 state 값입니다.");

	private final HttpStatus status;
	private final String code;
	private final String message;

	PlatformConnectionErrorCode(HttpStatus status, String code, String message) {
		this.status = status;
		this.code = code;
		this.message = message;
	}

	@Override
	public HttpStatus getStatus() {
		return status;
	}

	@Override
	public String getCode() {
		return code;
	}

	@Override
	public String getMessage() {
		return message;
	}
}
