package com.github.barbershop.provision.exception;

public class IncorrectTimeProvisionSlotException extends RuntimeException {
    public IncorrectTimeProvisionSlotException() {
        super("Время для бронирования не может быть раньше настоящего или начало не может равняться концу");
    }
}
