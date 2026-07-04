package com.github.barbershop.provision.exception;

public class InsufficientPermissionsToUpdateProvisionException extends RuntimeException {
    public InsufficientPermissionsToUpdateProvisionException() {
        super("Недостаточно прав на обновление этой услуги");
    }
}
