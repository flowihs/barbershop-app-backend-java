package com.github.barbershop.provision.dto;

import java.util.List;

import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

@Getter
public class UpdateProvisionRequest {
    private Long id;
    private String title;
    private String description;
    private Long provisionCategoryId;
    private String avatar;
    private List<MultipartFile> images;
}
