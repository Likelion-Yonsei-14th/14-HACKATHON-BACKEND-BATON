package com.likelion.yonsei.baton.domain.execution.exception;

import com.likelion.yonsei.baton.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum ExecutionErrorCode implements ErrorCode {

	EXECUTION_NOT_FOUND(HttpStatus.NOT_FOUND, "EXECUTION-001", "Execution을 찾을 수 없습니다."),
	RESOLVE_NOT_ALLOWED(HttpStatus.CONFLICT, "EXECUTION-002", "PENDING_REVIEW 상태의 BATON만 수동 처리할 수 있습니다."),
	INVALID_RESOLUTION(HttpStatus.BAD_REQUEST, "EXECUTION-003", "resolution_type에 필요한 값이 누락되었습니다.");

	private final HttpStatus status;
	private final String code;
	private final String message;

	ExecutionErrorCode(HttpStatus status, String code, String message) {
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
