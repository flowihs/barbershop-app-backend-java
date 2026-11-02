package com.github.barbershop.provision.exception;

import com.github.barbershop.common.error.exception.ProvisionException;
import org.springframework.http.HttpStatus;

public class ProvisionSlotNotFoundException extends ProvisionException {
    public ProvisionSlotNotFoundException() {
        super("Слот услуги не был найден", HttpStatus.NOT_FOUND);
    }
}
