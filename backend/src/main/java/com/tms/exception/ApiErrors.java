package com.tms.exception;

import com.tms.dto.response.Responses;
import java.time.Instant;
import java.util.*;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class ApiErrors {
  public static Responses.Error body(
      String code, String message, List<Responses.FieldError> fields) {
    return new Responses.Error(code, message, fields, UUID.randomUUID().toString(), Instant.now());
  }

  @ExceptionHandler(ApiException.class)
  ResponseEntity<?> api(ApiException e) {
    return ResponseEntity.status(e.status).body(body(e.code, e.getMessage(), List.of()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<?> validation(MethodArgumentNotValidException e) {
    return ResponseEntity.badRequest()
        .body(
            body(
                "VALIDATION_ERROR",
                "Check the highlighted fields",
                e.getBindingResult().getFieldErrors().stream()
                    .map(f -> new Responses.FieldError(f.getField(), f.getDefaultMessage()))
                    .toList()));
  }

  @ExceptionHandler({
    HttpMessageNotReadableException.class,
    MethodArgumentTypeMismatchException.class,
    org.springframework.web.bind.MissingRequestHeaderException.class
  })
  ResponseEntity<?> malformed(Exception e) {
    return ResponseEntity.badRequest()
        .body(body("INVALID_REQUEST", "Invalid request fields", List.of()));
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  ResponseEntity<?> conflict(Exception e) {
    return ResponseEntity.status(409)
        .body(
            body(
                "DATA_CONFLICT",
                "A duplicate record or overlapping schedule prevents this change",
                List.of()));
  }

  @ExceptionHandler(AccessDeniedException.class)
  ResponseEntity<?> denied(Exception e) {
    return ResponseEntity.status(403)
        .body(body("FORBIDDEN", "You do not have permission for this action", List.of()));
  }

  @ExceptionHandler(NoResourceFoundException.class)
  ResponseEntity<?> missing(Exception e) {
    return ResponseEntity.status(404).body(body("NOT_FOUND", "Resource not found", List.of()));
  }

  @ExceptionHandler(org.springframework.web.HttpRequestMethodNotSupportedException.class)
  ResponseEntity<Responses.Error> method(
      org.springframework.web.HttpRequestMethodNotSupportedException e) {
    return ResponseEntity.status(405)
        .body(body("METHOD_NOT_ALLOWED", "HTTP method not supported", List.of()));
  }

  @ExceptionHandler(Exception.class)
  ResponseEntity<?> unknown(Exception e) {
    var error = body("INTERNAL_ERROR", "Unable to complete the request", List.of());
    LoggerFactory.getLogger(ApiErrors.class)
        .error(
            "Request failed trace={} type={} stack={}",
            error.traceId(),
            e.getClass().getSimpleName(),
            Arrays.toString(e.getStackTrace()));
    return ResponseEntity.internalServerError().body(error);
  }
}
