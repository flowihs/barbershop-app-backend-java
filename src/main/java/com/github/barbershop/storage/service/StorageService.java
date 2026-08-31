package com.github.barbershop.storage.service;

import com.github.barbershop.storage.dto.UploadResult;
import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    UploadResult uploadImage(MultipartFile file);
}
