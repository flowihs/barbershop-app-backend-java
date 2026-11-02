package com.github.barbershop.provision.exception;

import com.github.barbershop.common.error.exception.StorageException;
import org.springframework.http.HttpStatus;

public class IncorrectTimeProvisionSlotException extends StorageException {
    public IncorrectTimeProvisionSlotException() {
        super("Время для бронирования не может быть раньше настоящего или начало не может равняться концу", HttpStatus.BAD_REQUEST);
    }
}
