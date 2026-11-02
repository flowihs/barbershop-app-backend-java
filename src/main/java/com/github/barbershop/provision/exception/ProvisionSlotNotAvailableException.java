package com.github.barbershop.provision.exception;

import com.github.barbershop.common.error.exception.ProvisionException;
import org.springframework.http.HttpStatus;

public class ProvisionSlotNotAvailableException extends ProvisionException {
    public ProvisionSlotNotAvailableException() {
        super("Слот уже заброванирован", HttpStatus.BAD_REQUEST);
    }
}
