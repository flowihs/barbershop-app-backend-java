package com.github.barbershop.provision.service;

import com.github.barbershop.account.entity.User;
import com.github.barbershop.account.service.UserService;
import com.github.barbershop.provision.dto.CreateProvisionRequest;
import com.github.barbershop.provision.dto.ProvisionResponse;
import com.github.barbershop.provision.entity.Provision;
import com.github.barbershop.provision.entity.ProvisionCategory;
import com.github.barbershop.provision.entity.ProvisionLike;
import com.github.barbershop.provision.exception.InsufficientPermissionsToCreateProvisionException;
import com.github.barbershop.provision.exception.ProvisionNotFoundException;
import com.github.barbershop.provision.repository.ProvisionLikeRepository;
import com.github.barbershop.provision.repository.ProvisionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProvisionService {
    private final ProvisionRepository provisionRepository;
    private final ProvisionCategoryService provisionCategoryService;
    private final UserService userService;
    private final ProvisionLikeRepository provisionLikeRepository;

    public List<ProvisionResponse> getAll() {
        List<Provision> provisions = provisionRepository.findAll();

        if (provisions.isEmpty()) {
            throw new ProvisionNotFoundException();
        }

        return provisions.stream()
                .map(ProvisionResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public ProvisionResponse getById(Long id, Long userId) {
        ProvisionResponse response = ProvisionResponse.fromEntity(provisionRepository.findById(id)
                .orElseThrow(ProvisionNotFoundException::new));

        response.setLikesCount(provisionLikeRepository.countByProvisionId(response.getId()));
        response.setLikedByMe(provisionLikeRepository.existsByProvisionIdAndUserId(response.getId(), userId));

        return response;
    }

    @Transactional
    public ProvisionResponse create(CreateProvisionRequest dto, Long userId) {
        User user = userService.findById(userId);

        String role = user.getRole().toString();
        if (!"ADMIN".equals(role) && !"BARBER".equals(role)) {
            throw new InsufficientPermissionsToCreateProvisionException();
        }

        ProvisionCategory provisionCategory = provisionCategoryService.getEntityById(dto.getCategoryId());

        Provision provision = Provision.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .provisionCategory(provisionCategory)
                .user(user)
                .build();

        Provision saved = provisionRepository.save(provision);
        ProvisionResponse response = ProvisionResponse.fromEntity(saved);
        response.setLikesCount(0);
        return response;
    }

    @Transactional
    public void toggleLike(final Long provisionId, final Long userId) {
        Provision provision = provisionRepository.findById(provisionId)
                .orElseThrow(ProvisionNotFoundException::new);
        User user = userService.findById(userId);

        provisionLikeRepository.findByProvisionIdAndUserId(provisionId, userId)
                .ifPresentOrElse(
                        provisionLikeRepository::delete,
                        () -> provisionLikeRepository.save(ProvisionLike.builder()
                                .provision(provision)
                                .user(user)
                                .build())
                );
    }
}
