package com.github.barbershop.provision.exception;

public class InsufficientPermissionsToCancelBookingException extends RuntimeException {
    public InsufficientPermissionsToCancelBookingException() {
        super("Недостаточно прав для отмены этой записи");
    }
}
