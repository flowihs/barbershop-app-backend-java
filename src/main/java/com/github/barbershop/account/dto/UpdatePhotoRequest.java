package com.github.barbershop.account.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class UpdatePhotoRequest {
    private Long id;
    private MultipartFile photo;
}
