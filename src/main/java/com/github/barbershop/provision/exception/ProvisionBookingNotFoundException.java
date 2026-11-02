package com.github.barbershop.provision.exception;

import com.github.barbershop.common.error.exception.ProvisionException;
import org.springframework.http.HttpStatus;

public class ProvisionBookingNotFoundException extends ProvisionException {
    public ProvisionBookingNotFoundException() {
        super("Бронирование не было найдено", HttpStatus.NOT_FOUND);
    }
}
