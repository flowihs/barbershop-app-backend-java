package com.github.barbershop.provision.exception;

import com.github.barbershop.common.error.exception.StorageException;
import org.springframework.http.HttpStatus;

public class InsufficientPermissionsToCancelBookingException extends StorageException {
    public InsufficientPermissionsToCancelBookingException() {
        super("Недостаточно прав для отмены этой записи", HttpStatus.BAD_REQUEST);
    }
}
