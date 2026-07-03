package com.github.barbershop.provision.exception;

public class ProvisionSlotNotFoundException extends RuntimeException {
    public ProvisionSlotNotFoundException() {
        super("Слот услуги не был найден");
    }
}
