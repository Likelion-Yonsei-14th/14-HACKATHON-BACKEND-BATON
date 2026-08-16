package com.likelion.yonsei.baton.integration.openai.exception;

import com.likelion.yonsei.baton.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum OpenAiErrorCode implements ErrorCode {

	MISCONFIGURED(HttpStatus.INTERNAL_SERVER_ERROR, "OPENAI-001", "OpenAI API 설정이 올바르지 않습니다."),
	UNAUTHORIZED(HttpStatus.BAD_GATEWAY, "OPENAI-002", "OpenAI API 인증에 실패했습니다."),
	RATE_LIMITED(HttpStatus.BAD_GATEWAY, "OPENAI-003", "OpenAI API 요청 한도를 초과했습니다."),
	INVALID_REQUEST(HttpStatus.BAD_GATEWAY, "OPENAI-004", "OpenAI 요청이 올바르지 않습니다."),
	TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, "OPENAI-005", "OpenAI 응답 시간이 초과되었습니다."),
	UPSTREAM_ERROR(HttpStatus.BAD_GATEWAY, "OPENAI-006", "OpenAI 서버에서 오류가 발생했습니다."),
	REQUEST_FAILED(HttpStatus.BAD_GATEWAY, "OPENAI-007", "OpenAI 요청 처리 중 오류가 발생했습니다."),
	EMPTY_RESPONSE(HttpStatus.BAD_GATEWAY, "OPENAI-008", "OpenAI 응답이 비어 있습니다.");

	private final HttpStatus status;
	private final String code;
	private final String message;

	OpenAiErrorCode(HttpStatus status, String code, String message) {
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
