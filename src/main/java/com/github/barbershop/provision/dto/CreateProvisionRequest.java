package com.github.barbershop.provision.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
public class CreateProvisionRequest {
    @NotBlank
    private String title;

    @NotBlank
    private String description;

    @NotBlank 
    private String avatar;

    @NotBlank 
    private List<MultipartFile> images;

    @NotBlank
    private Long categoryId;

    List<CreateProvisionSlotRequest> slots;
}
