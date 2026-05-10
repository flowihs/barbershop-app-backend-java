package com.github.barbershop.provision.service;

import com.github.barbershop.account.entity.UserRole;
import com.github.barbershop.account.security.RequireRole;
import com.github.barbershop.provision.dto.CreateProvisionRequest;
import com.github.barbershop.provision.dto.ProvisionResponse;
import com.github.barbershop.provision.entity.Provision;
import com.github.barbershop.provision.exception.ProvisionNotFoundException;
import com.github.barbershop.provision.repository.ProvisionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProvisionService {
    private final ProvisionRepository provisionRepository;

    public List<ProvisionResponse> getAll() {
        List<Provision> provisions = provisionRepository.findAll();

        if (provisions.isEmpty()) {
            throw new ProvisionNotFoundException();
        }

        return provisions.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ProvisionResponse getById(Long id) {
        return mapToResponse(provisionRepository.findById(id)
                .orElseThrow(ProvisionNotFoundException::new));
    }

//    public List<ProvisionResponse> getByCategory(Long )

    private ProvisionResponse mapToResponse(Provision provision) {
        return ProvisionResponse.builder()
                .id(provision.getId())
                .title(provision.getTitle())
                .description(provision.getDescription())
                .rating(provision.getRating())
                .category(provision.getCategory())
                .build();
    }
}
