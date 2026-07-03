package com.github.barbershop.provision.dto;

import lombok.Getter;

@Getter
public class UpdateProvisionRequest {
    private Long id;
    private String title;
    private String description;
    private Long provisionCategoryId;
}
