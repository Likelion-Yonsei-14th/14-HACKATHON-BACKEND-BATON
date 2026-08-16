package com.likelion.yonsei.baton.common.exception;

import com.likelion.yonsei.baton.common.response.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
		ErrorCode errorCode = e.getErrorCode();
		log.warn("BusinessException: code={}, message={}", errorCode.getCode(), e.getMessage());
		return ResponseEntity
				.status(errorCode.getStatus())
				.body(ApiResponse.error(errorCode.getCode(), e.getMessage()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException e) {
		String details = e.getBindingResult().getAllErrors().stream()
				.map(err -> {
					if (err instanceof FieldError fe) {
						return fe.getField() + ": " + fe.getDefaultMessage();
					}
					return err.getDefaultMessage();
				})
				.collect(Collectors.joining(", "));

		ErrorCode code = CommonErrorCode.INVALID_INPUT;
		String message = details.isBlank() ? code.getMessage() : details;

		log.warn("Validation failed: {}", message);
		return ResponseEntity
				.status(code.getStatus())
				.body(ApiResponse.error(code.getCode(), message));
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiResponse<Void>> handleMessageNotReadable(HttpMessageNotReadableException e) {
		log.warn("Malformed request body: {}", e.getMessage());
		ErrorCode code = CommonErrorCode.INVALID_INPUT;
		return ResponseEntity
				.status(code.getStatus())
				.body(ApiResponse.error(code.getCode(), "요청 본문을 해석할 수 없습니다."));
	}

	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ResponseEntity<ApiResponse<Void>> handleMissingParameter(MissingServletRequestParameterException e) {
		log.warn("Missing required parameter: {}", e.getParameterName());
		ErrorCode code = CommonErrorCode.INVALID_INPUT;
		return ResponseEntity
				.status(code.getStatus())
				.body(ApiResponse.error(code.getCode(), "필수 파라미터가 누락되었습니다: " + e.getParameterName()));
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
		log.warn("Type mismatch for parameter '{}': {}", e.getName(), e.getMessage());
		ErrorCode code = CommonErrorCode.INVALID_INPUT;
		return ResponseEntity
				.status(code.getStatus())
				.body(ApiResponse.error(code.getCode(), "잘못된 형식의 파라미터입니다: " + e.getName()));
	}

	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<ApiResponse<Void>> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
		log.warn("Method not supported: {}", e.getMessage());
		ErrorCode code = CommonErrorCode.METHOD_NOT_ALLOWED;
		return ResponseEntity
				.status(code.getStatus())
				.body(ApiResponse.error(code.getCode(), code.getMessage()));
	}

	@ExceptionHandler(NoResourceFoundException.class)
	public ResponseEntity<ApiResponse<Void>> handleNoResourceFound(NoResourceFoundException e) {
		log.debug("No static resource: {}", e.getResourcePath());
		ErrorCode code = CommonErrorCode.RESOURCE_NOT_FOUND;
		return ResponseEntity
				.status(code.getStatus())
				.body(ApiResponse.error(code.getCode(), code.getMessage()));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception e) {
		log.error("Unexpected exception", e);
		ErrorCode code = CommonErrorCode.INTERNAL_ERROR;
		return ResponseEntity
				.status(code.getStatus())
				.body(ApiResponse.error(code.getCode(), code.getMessage()));
	}
}
