package com.github.barbershop.provision.exception;

import com.github.barbershop.common.error.exception.ProvisionException;
import org.springframework.http.HttpStatus;

public class ProvisionCategoryNotFoundException extends ProvisionException {
    public ProvisionCategoryNotFoundException() {
        super(
                "Категории не были найдены", HttpStatus.NOT_FOUND
        );
    }
}
