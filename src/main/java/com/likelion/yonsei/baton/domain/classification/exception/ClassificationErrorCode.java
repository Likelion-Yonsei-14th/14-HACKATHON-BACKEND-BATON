package com.likelion.yonsei.baton.domain.classification.exception;

import com.likelion.yonsei.baton.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum ClassificationErrorCode implements ErrorCode {

	CLASSIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "CLASSIFICATION-001", "Classification을 찾을 수 없습니다.");

	private final HttpStatus status;
	private final String code;
	private final String message;

	ClassificationErrorCode(HttpStatus status, String code, String message) {
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
