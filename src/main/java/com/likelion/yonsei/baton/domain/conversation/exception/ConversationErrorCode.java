package com.likelion.yonsei.baton.domain.conversation.exception;

import com.likelion.yonsei.baton.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum ConversationErrorCode implements ErrorCode {

	CONVERSATION_NOT_FOUND(HttpStatus.NOT_FOUND, "CONVERSATION-001", "대화를 찾을 수 없습니다.");

	private final HttpStatus status;
	private final String code;
	private final String message;

	ConversationErrorCode(HttpStatus status, String code, String message) {
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
