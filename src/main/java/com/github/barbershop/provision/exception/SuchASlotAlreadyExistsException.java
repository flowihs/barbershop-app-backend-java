package com.github.barbershop.provision.exception;

public class SuchASlotAlreadyExistsException extends RuntimeException {
    public SuchASlotAlreadyExistsException() {
        super("Такой слот уже существует");
    }
}
