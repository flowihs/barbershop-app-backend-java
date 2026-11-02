package com.github.barbershop.common.error.exception;

import org.springframework.http.HttpStatus;

public class CategoryException extends RuntimeException {
    private final HttpStatus status;

    public CategoryException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
