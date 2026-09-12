package com.github.barbershop.provision.exception;

public class ProvisionBookingNotFoundException extends RuntimeException {
    public ProvisionBookingNotFoundException() {
        super("Бронирование не было найдено");
    }
}
