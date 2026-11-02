package com.github.barbershop.common.error;

import com.github.barbershop.account.exception.UnauthorizedException;
import com.github.barbershop.common.error.dto.ErrorResponse;
import com.github.barbershop.common.error.exception.CategoryException;
import com.github.barbershop.common.error.exception.StorageException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CategoryException.class)
    public ResponseEntity<?> handleCategoryErrors(CategoryException exception, WebRequest request) {
        ErrorResponse body = new ErrorResponse(
                LocalDateTime.now(),
                exception.getStatus().value(),
                "Category error",
                exception.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );

        return ResponseEntity
                .status(exception.getStatus())
                .body(body);
    }

    @ExceptionHandler(StorageException.class)
    public ResponseEntity<?> handleStorageException(StorageException exception, WebRequest request) {
        ErrorResponse body = new ErrorResponse(
                LocalDateTime.now(),
                exception.getStatus().value(),
                "Storage error",
                exception.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );

        return ResponseEntity
                .status(exception.getStatus())
                .body(body);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Map<String, Object>> handleUnauthorizedException(UnauthorizedException e) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Map.of(
                        "error", e.getMessage(),
                        "timestamp", LocalDateTime.now()
                ));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDeniedException(AccessDeniedException e) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(Map.of(
                        "error", e.getMessage(),
                        "timestamp", LocalDateTime.now()
                ));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "error", e.getMessage(),
                        "timestamp", LocalDateTime.now()
                ));
    }
}
