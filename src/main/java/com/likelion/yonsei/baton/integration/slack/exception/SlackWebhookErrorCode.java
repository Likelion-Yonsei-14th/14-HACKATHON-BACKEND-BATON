package com.likelion.yonsei.baton.integration.slack.exception;

import com.likelion.yonsei.baton.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum SlackWebhookErrorCode implements ErrorCode {

	INVALID_SLACK_SIGNATURE(HttpStatus.BAD_REQUEST, "INVALID_SLACK_SIGNATURE", "Slack 요청을 검증할 수 없습니다."),
	SLACK_EVENT_PROCESSING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "SLACK_EVENT_PROCESSING_FAILED", "Slack 이벤트 처리 중 오류가 발생했습니다.");

	private final HttpStatus status;
	private final String code;
	private final String message;

	SlackWebhookErrorCode(HttpStatus status, String code, String message) {
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
