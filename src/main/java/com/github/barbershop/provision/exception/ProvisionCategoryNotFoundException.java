package com.github.barbershop.provision.exception;

public class ProvisionCategoryNotFoundException extends RuntimeException {
    public ProvisionCategoryNotFoundException() {
        super(
                "Категории не были найдены"
        );
    }
}
