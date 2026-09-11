package com.github.barbershop.provision.exception;

public class ProvisionReviewNotFoundException extends RuntimeException {
    public ProvisionReviewNotFoundException() {
        super("Отзыв не был найден");
    }
}
