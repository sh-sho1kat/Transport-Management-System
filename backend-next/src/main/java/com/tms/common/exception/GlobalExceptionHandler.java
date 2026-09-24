package com.tms.common.exception;

import com.tms.common.response.ApiErrorFactory;
import com.tms.common.response.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
  private final ApiErrorFactory errors;

  public GlobalExceptionHandler(ApiErrorFactory errors) {
    this.errors = errors;
  }

  @Override
  protected ResponseEntity<Object> handleExceptionInternal(
      Exception ex, Object body, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
    String code =
        switch (status.value()) {
          case 400 -> "INVALID_REQUEST";
          case 404 -> "NOT_FOUND";
          case 405 -> "METHOD_NOT_ALLOWED";
          default -> "REQUEST_REJECTED";
        };
    String message =
        switch (status.value()) {
          case 400 -> "Request validation failed";
          case 404 -> "Resource not found";
          case 405 -> "HTTP method not allowed";
          default -> "Request could not be processed";
        };
    return new ResponseEntity<>(
        errors.create(((ServletWebRequest) request).getRequest(), status.value(), code, message),
        headers,
        status);
  }

  @ExceptionHandler(AccessDeniedException.class)
  ResponseEntity<ApiErrorResponse> forbidden(
      AccessDeniedException exception, HttpServletRequest request) {
    return ResponseEntity.status(403)
        .body(errors.create(request, 403, "FORBIDDEN", "Access denied"));
  }

  @ExceptionHandler(Exception.class)
  ResponseEntity<ApiErrorResponse> unexpected(Exception exception, HttpServletRequest request) {
    log.error("Unhandled request failure", exception);
    return ResponseEntity.internalServerError()
        .body(errors.create(request, 500, "INTERNAL_ERROR", "An unexpected error occurred"));
  }
}
