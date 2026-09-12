package com.github.barbershop.provision.exception;

public class ProvisionSlotNotAvailableException extends RuntimeException {
    public ProvisionSlotNotAvailableException() {
        super("Слот уже заброванирован");
    }
}
