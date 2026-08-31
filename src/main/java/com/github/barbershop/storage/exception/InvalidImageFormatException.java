package com.github.barbershop.storage.exception;

public class InvalidImageFormatException extends RuntimeException {
    public InvalidImageFormatException() {
        super("Файл не является изображением или поврежден");
    }
}
