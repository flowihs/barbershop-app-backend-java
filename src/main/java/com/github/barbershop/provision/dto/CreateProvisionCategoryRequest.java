package com.github.barbershop.provision.dto;

import com.github.barbershop.provision.entity.ProvisionCategory;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CreateProvisionCategoryRequest {
    @NotBlank
    private String name;

    @NotBlank
    private String description;
}
