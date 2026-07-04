package com.github.barbershop.common.error.dto;

import java.time.LocalDateTime;

public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String messages,
        String path
) {

}
