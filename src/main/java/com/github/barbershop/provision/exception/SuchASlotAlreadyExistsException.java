package com.github.barbershop.provision.exception;

import com.github.barbershop.common.error.exception.ProvisionException;
import org.springframework.http.HttpStatus;

public class SuchASlotAlreadyExistsException extends ProvisionException {
    public SuchASlotAlreadyExistsException() {
        super("Такой слот уже существует", HttpStatus.CONFLICT);
    }
}
