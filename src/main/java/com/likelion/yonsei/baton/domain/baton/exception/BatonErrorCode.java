package com.likelion.yonsei.baton.domain.baton.exception;

import com.likelion.yonsei.baton.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum BatonErrorCode implements ErrorCode {

	BATON_NOT_FOUND(HttpStatus.NOT_FOUND, "BATON-001", "BATON을 찾을 수 없습니다."),
	INVALID_STATUS_TRANSITION(HttpStatus.CONFLICT, "BATON-002", "현재 상태에서 허용되지 않는 작업입니다."),
	TRIGGER_MESSAGE_ALREADY_USED(HttpStatus.CONFLICT, "BATON-003", "이미 다른 BATON이 연결된 메시지입니다.");

	private final HttpStatus status;
	private final String code;
	private final String message;

	BatonErrorCode(HttpStatus status, String code, String message) {
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
