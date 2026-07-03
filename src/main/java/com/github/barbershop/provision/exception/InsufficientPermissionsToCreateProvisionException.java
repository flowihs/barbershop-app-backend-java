package com.github.barbershop.provision.exception;

public class InsufficientPermissionsToCreateProvisionException extends RuntimeException {
    public InsufficientPermissionsToCreateProvisionException() {
        super("У пользователя недостаточно прав для создания услуги");
    }
}
