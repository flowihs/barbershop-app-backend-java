package com.github.barbershop.provision.exception;

import com.github.barbershop.common.error.exception.ProvisionException;
import org.springframework.http.HttpStatus;

public class ProvisionNotFoundException extends ProvisionException {
    public ProvisionNotFoundException() {
        super("Услуга не была найдена", HttpStatus.BAD_REQUEST);
    }
}
