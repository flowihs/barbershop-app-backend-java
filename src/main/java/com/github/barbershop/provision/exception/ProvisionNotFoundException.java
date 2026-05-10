package com.github.barbershop.provision.exception;

public class ProvisionNotFoundException extends RuntimeException {
    public ProvisionNotFoundException() {
        super("Услуга не была найдена");
    }
}
