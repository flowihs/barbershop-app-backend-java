package com.github.barbershop.account.dto;

import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

@Getter
public class UpdatePhotoRequest {
    private Long id;
    private MultipartFile photo;
}
