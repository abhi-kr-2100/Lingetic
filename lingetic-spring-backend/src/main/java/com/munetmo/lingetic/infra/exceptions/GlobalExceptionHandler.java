package com.munetmo.lingetic.infra.exceptions;

import com.munetmo.lingetic.LanguageTestService.Exceptions.QuestionNotFoundException;
import com.munetmo.lingetic.LanguageTestService.Exceptions.QuestionWithIDAlreadyExistsException;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(QuestionNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleQuestionNotFoundException(
            QuestionNotFoundException ex, WebRequest request) {
        return buildErrorResponse(
                HttpStatus.NOT_FOUND,
                ex.getClass().getSimpleName(),
                ex.getMessage() != null ? ex.getMessage() : "Question not found"
        );
    }

    @ExceptionHandler(QuestionWithIDAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleQuestionWithIDAlreadyExistsException(
            QuestionWithIDAlreadyExistsException ex, WebRequest request) {
        return buildErrorResponse(
                HttpStatus.CONFLICT,
                ex.getClass().getSimpleName(),
                ex.getMessage() != null ? ex.getMessage() : "Question with ID already exists"
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                ex.getClass().getSimpleName(),
                ex.getMessage() != null ? ex.getMessage() : "Invalid argument provided"
        );
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuthenticationException(
            AuthenticationException ex, WebRequest request) {
        return buildErrorResponse(
                HttpStatus.UNAUTHORIZED,
                ex.getClass().getSimpleName(),
                ex.getMessage() != null ? ex.getMessage() : "Authentication failed"
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDeniedException(
            AccessDeniedException ex, WebRequest request) {
        return buildErrorResponse(
                HttpStatus.FORBIDDEN,
                ex.getClass().getSimpleName(),
                ex.getMessage() != null ? ex.getMessage() : "Access denied"
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(
            Exception ex, WebRequest request) {
        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ex.getClass().getSimpleName(),
                ex.getMessage() != null ? ex.getMessage() : "An unexpected error occurred"
        );
    }

    private ResponseEntity<Map<String, Object>> buildErrorResponse(
            HttpStatus status, String error, @Nullable String message) {
        Map<String, Object> errorDetails = new LinkedHashMap<>();
        errorDetails.put("timestamp", Instant.now().toString());
        errorDetails.put("status", status.value());
        errorDetails.put("error", error);
        errorDetails.put("message", message);

        return ResponseEntity.status(status).body(errorDetails);
    }
}
