package com.github.barbershop.storage.exception;

import com.github.barbershop.common.error.exception.StorageException;
import org.springframework.http.HttpStatus;

public class InvalidImageFormatException extends StorageException {
    public InvalidImageFormatException() {
        super("Файл не является изображением или поврежден", HttpStatus.BAD_REQUEST);
    }
}
