package com.github.barbershop.provision.exception;

import com.github.barbershop.common.error.exception.ProvisionException;
import org.springframework.http.HttpStatus;

public class InsufficientPermissionsToCreateProvisionException extends ProvisionException {
    public InsufficientPermissionsToCreateProvisionException() {
        super("У пользователя недостаточно прав для создания услуги", HttpStatus.BAD_REQUEST);
    }
}
