package com.likelion.yonsei.baton.integration.localllm.exception;

import com.likelion.yonsei.baton.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum LocalLlmErrorCode implements ErrorCode {

	TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, "LOCALLLM-001", "로컬 모델 응답 시간이 초과되었습니다."),
	UPSTREAM_ERROR(HttpStatus.BAD_GATEWAY, "LOCALLLM-002", "로컬 모델 서버에서 오류가 발생했습니다."),
	REQUEST_FAILED(HttpStatus.BAD_GATEWAY, "LOCALLLM-003", "로컬 모델 요청 처리 중 오류가 발생했습니다."),
	EMPTY_RESPONSE(HttpStatus.BAD_GATEWAY, "LOCALLLM-004", "로컬 모델 응답이 비어 있습니다.");

	private final HttpStatus status;
	private final String code;
	private final String message;

	LocalLlmErrorCode(HttpStatus status, String code, String message) {
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
