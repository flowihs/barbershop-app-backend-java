package com.github.barbershop.provision.exception;

import com.github.barbershop.common.error.exception.ProvisionException;
import org.springframework.http.HttpStatus;

public class InsufficientPermissionsToCreateProvisionCategoryException extends ProvisionException {
    public InsufficientPermissionsToCreateProvisionCategoryException() {
        super("Недостаточно прав для создания категории", HttpStatus.BAD_REQUEST);
    }
}
