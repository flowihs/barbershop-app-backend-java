package com.github.barbershop.storage.service;

import com.github.barbershop.storage.dto.UploadResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface StorageService {
    UploadResult uploadImage(MultipartFile file);

    List<UploadResult> uploadImages(List<MultipartFile> files);
}
