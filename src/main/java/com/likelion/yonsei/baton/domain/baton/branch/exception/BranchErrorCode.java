package com.likelion.yonsei.baton.domain.baton.branch.exception;

import com.likelion.yonsei.baton.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum BranchErrorCode implements ErrorCode {

	BRANCH_NOT_FOUND(HttpStatus.NOT_FOUND, "BRANCH-001", "Branch를 찾을 수 없습니다."),
	BRANCH_MUTATION_NOT_ALLOWED(HttpStatus.CONFLICT, "BRANCH-002", "DRAFT 상태의 BATON에서만 Branch를 추가·삭제할 수 있습니다.");

	private final HttpStatus status;
	private final String code;
	private final String message;

	BranchErrorCode(HttpStatus status, String code, String message) {
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
