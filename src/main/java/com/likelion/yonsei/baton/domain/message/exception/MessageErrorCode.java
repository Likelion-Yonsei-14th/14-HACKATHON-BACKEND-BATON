package com.likelion.yonsei.baton.domain.message.exception;

import com.likelion.yonsei.baton.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum MessageErrorCode implements ErrorCode {

	MESSAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "MESSAGE-001", "메시지를 찾을 수 없습니다."),
	MESSAGE_SEND_FAILED(HttpStatus.BAD_GATEWAY, "MESSAGE-002", "메시지 발송에 실패했습니다.");

	private final HttpStatus status;
	private final String code;
	private final String message;

	MessageErrorCode(HttpStatus status, String code, String message) {
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
