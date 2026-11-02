package com.github.barbershop.provision.exception;

import com.github.barbershop.common.error.exception.ProvisionException;
import com.github.barbershop.provision.entity.Provision;
import org.springframework.http.HttpStatus;

public class InsufficientPermissionsToDeleteProvisionException extends ProvisionException {
    public InsufficientPermissionsToDeleteProvisionException() {
        super("Недостаточно прав для удаления услуги", HttpStatus.BAD_REQUEST);
    }
}
