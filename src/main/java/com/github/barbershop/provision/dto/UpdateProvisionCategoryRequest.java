package com.github.barbershop.provision.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

@Getter
public class UpdateProvisionCategoryRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String description;

    @NotBlank
    private MultipartFile image;
}
