package com.github.barbershop.provision.exception;

import com.github.barbershop.common.error.exception.ProvisionException;
import org.springframework.http.HttpStatus;

public class InsufficientPermissionsToUpdateProvisionException extends ProvisionException {
    public InsufficientPermissionsToUpdateProvisionException() {
        super("Недостаточно прав на обновление этой услуги", HttpStatus.BAD_REQUEST);
    }
}
