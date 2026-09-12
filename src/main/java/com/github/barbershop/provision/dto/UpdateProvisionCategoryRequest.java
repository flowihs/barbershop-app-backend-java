package com.github.barbershop.provision.dto;

import com.github.barbershop.provision.entity.ProvisionCategory;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Builder
public class UpdateProvisionCategoryRequest {
    @NotBlank
    private Long id;

    private String name;
    private String description;
    private MultipartFile image;
}
