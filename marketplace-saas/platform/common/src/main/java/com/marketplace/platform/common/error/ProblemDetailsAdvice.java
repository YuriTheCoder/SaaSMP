package com.marketplace.platform.common.error;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@ControllerAdvice
public class ProblemDetailsAdvice {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        String traceId = UUID.randomUUID().toString();
        Map<String, Object> body = new HashMap<>();
        body.put("type", "https://datatracker.ietf.org/doc/html/rfc7807");
        body.put("title", "Validation failed");
        body.put("status", 422);
        body.put("detail", ex.getMessage());
        body.put("instance", "/validation");
        body.put("traceId", traceId);
        return ResponseEntity.unprocessableEntity()
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(body);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleStatus(ResponseStatusException ex) {
        String traceId = UUID.randomUUID().toString();
        Map<String, Object> body = new HashMap<>();
        body.put("type", URI.create("about:blank").toString());
        body.put("title", ex.getReason());
        body.put("status", ex.getStatusCode().value());
        body.put("detail", ex.getMessage());
        body.put("traceId", traceId);
        return ResponseEntity.status(ex.getStatusCode())
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        String traceId = UUID.randomUUID().toString();
        Map<String, Object> body = new HashMap<>();
        body.put("type", URI.create("about:blank").toString());
        body.put("title", "Internal Server Error");
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("detail", ex.getMessage());
        body.put("traceId", traceId);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(body);
    }
}


