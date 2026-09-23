package com.tms.shared.exception;

import com.tms.shared.response.ApiError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class})
  ResponseEntity<ApiError> invalid(Exception exception) {
    return ResponseEntity.badRequest()
        .body(ApiError.of("INVALID_REQUEST", "Request validation failed"));
  }

  @ExceptionHandler(Exception.class)
  ResponseEntity<ApiError> unexpected(Exception exception) {
    log.error("Unhandled request failure", exception);
    return ResponseEntity.internalServerError()
        .body(ApiError.of("INTERNAL_ERROR", "An unexpected error occurred"));
  }
}
