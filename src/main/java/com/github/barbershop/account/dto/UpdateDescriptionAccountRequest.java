package com.github.barbershop.account.dto;

import lombok.Getter;

@Getter
public class UpdateDescriptionAccountRequest {
    private Long id;
    private String description;
}
