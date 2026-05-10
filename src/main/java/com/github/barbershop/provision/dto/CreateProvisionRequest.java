package com.github.barbershop.provision.dto;

import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class CreateProvisionRequest {
    @NotBlank
    private String title;

    @NotBlank
    private String description;

    @NotBlank
    private float rating;

    @NotBlank
    private Long categoryId;
}
