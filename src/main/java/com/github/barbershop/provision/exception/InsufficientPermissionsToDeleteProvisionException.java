package com.github.barbershop.provision.exception;

public class InsufficientPermissionsToDeleteProvisionException extends RuntimeException {
    public InsufficientPermissionsToDeleteProvisionException() {
        super("Недостаточно прав для удаления услуги");
    }
}
