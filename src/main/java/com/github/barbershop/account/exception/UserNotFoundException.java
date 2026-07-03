package com.github.barbershop.account.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException() {
        super("Пользователь не был найден");
    }
}
