package com.github.barbershop.account.exception;

public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException() {
        super("Пользователь не авторизован");
    }

    public UnauthorizedException(String message) {
        super(message);
    }
}
