package com.github.barbershop.provision.service;

import com.github.barbershop.provision.dto.ProvisionReviewResponse;
import com.github.barbershop.provision.exception.ProvisionReviewNotFoundException;
import com.github.barbershop.provision.repository.ProvisionReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProvisionReviewService {
    private final ProvisionReviewRepository provisionReviewRepository;

    public ProvisionReviewResponse getById(final Long id) {
        return ProvisionReviewResponse.fromEntity(provisionReviewRepository.findById(id)
                .orElseThrow(ProvisionReviewNotFoundException::new));
    }
}
