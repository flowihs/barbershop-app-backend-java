package com.github.barbershop.provision.service;

import com.github.barbershop.account.entity.UserRole;
import com.github.barbershop.provision.dto.CreateProvisionCategoryRequest;
import com.github.barbershop.provision.dto.ProvisionCategoryResponse;
import com.github.barbershop.provision.entity.ProvisionCategory;
import com.github.barbershop.provision.exception.InsufficientPermissionsToCreateProvisionCategoryException;
import com.github.barbershop.provision.exception.ProvisionCategoryNotFoundException;
import com.github.barbershop.provision.exception.ProvisionNotFoundException;
import com.github.barbershop.provision.repository.ProvisionCategoryRepository;
import com.github.barbershop.storage.dto.UploadResult;
import com.github.barbershop.storage.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProvisionCategoryService {
    private final ProvisionCategoryRepository provisionCategoryRepository;
    private final StorageService storageService;

    public List<ProvisionCategoryResponse> getAll() {
        List<ProvisionCategory> categories = provisionCategoryRepository.findAll();

        if (categories.isEmpty()) {
            throw new ProvisionCategoryNotFoundException();
        }

        return categories.stream()
                .map(ProvisionCategoryResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public ProvisionCategory getEntityById(Long id) {
        return provisionCategoryRepository.findById(id)
                .orElseThrow(ProvisionNotFoundException::new);
    }

    @Transactional
    public ProvisionCategoryResponse create(CreateProvisionCategoryRequest dto, UserRole roleUser) {
        if (!Objects.equals(roleUser.toString(), "ADMIN")) {
            throw new InsufficientPermissionsToCreateProvisionCategoryException();
        }

        UploadResult upload = storageService.uploadImage(dto.getImage());

        ProvisionCategory provisionCategory = ProvisionCategory.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .image(upload.publicUrl())
                .build();

        ProvisionCategory createProvisionCategory = provisionCategoryRepository.save(provisionCategory);

        return ProvisionCategoryResponse.fromEntity(createProvisionCategory);
    }
}
