package com.github.barbershop.provision.exception;

public class InsufficientPermissionsToCreateProvisionCategoryException extends RuntimeException {
    public InsufficientPermissionsToCreateProvisionCategoryException() {
        super("Недостаточно прав для создания категории");
    }
}
