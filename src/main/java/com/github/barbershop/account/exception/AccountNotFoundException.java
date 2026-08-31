package com.github.barbershop.account.exception;

public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException() {
        super("Пользователь не был найден");
    }
}
