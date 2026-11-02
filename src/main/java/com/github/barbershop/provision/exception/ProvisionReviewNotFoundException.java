package com.github.barbershop.provision.exception;

import com.github.barbershop.common.error.exception.ProvisionException;
import org.springframework.http.HttpStatus;

public class ProvisionReviewNotFoundException extends ProvisionException {
    public ProvisionReviewNotFoundException() {
        super("Отзыв не был найден", HttpStatus.NOT_FOUND);
    }
}
