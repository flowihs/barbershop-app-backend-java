package com.github.barbershop.provision.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

import java.util.List;

@Getter
public class CreateProvisionRequest {
    @NotBlank
    private String title;

    @NotBlank
    private String description;

    @NotBlank
    private Long categoryId;

    List<CreateProvisionSlotRequest> slots;
}
