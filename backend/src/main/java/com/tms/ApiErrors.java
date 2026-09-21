package com.tms;

import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.util.HtmlUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class ApiErrors {
    @ExceptionHandler({NoHandlerFoundException.class, HttpRequestMethodNotSupportedException.class})
    ResponseEntity<?> notFound(Exception error, HttpServletRequest request) {
        String path = HtmlUtils.htmlEscape(request.getRequestURI());
        String html = "<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n<meta charset=\"utf-8\">\n<title>Error</title>\n</head>\n<body>\n<pre>Cannot "
                + request.getMethod() + " " + path + "</pre>\n</body>\n</html>\n";
        return ResponseEntity.status(404).contentType(MediaType.TEXT_HTML).body(html);
    }
    @ExceptionHandler(ApiException.class)
    ResponseEntity<?> api(ApiException error) {
        return ResponseEntity.status(error.status).body(Map.of(error.key, error.getMessage()));
    }
}
