package com.github.barbershop.provision.service;

import com.github.barbershop.account.entity.User;
import com.github.barbershop.account.service.UserService;
import com.github.barbershop.provision.dto.*;
import com.github.barbershop.provision.entity.Provision;
import com.github.barbershop.provision.entity.ProvisionCategory;
import com.github.barbershop.provision.entity.ProvisionLike;
import com.github.barbershop.provision.entity.ProvisionSlot;
import com.github.barbershop.provision.exception.*;
import com.github.barbershop.provision.repository.ProvisionLikeRepository;
import com.github.barbershop.provision.repository.ProvisionRepository;
import com.github.barbershop.provision.repository.ProvisionSlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProvisionService {
    private final ProvisionRepository provisionRepository;
    private final ProvisionCategoryService provisionCategoryService;
    private final UserService userService;
    private final ProvisionLikeRepository provisionLikeRepository;
    private final ProvisionSlotRepository provisionSlotRepository; // используется в create()

    public List<ProvisionResponse> getAll() {
        List<Provision> provisions = provisionRepository.findAll();

        if (provisions.isEmpty()) {
            throw new ProvisionNotFoundException();
        }

        return provisions.stream()
                .map(ProvisionResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public ProvisionResponse getById(final Long id, final Long userId) {
        Provision provision = provisionRepository.findById(id)
                .orElseThrow(ProvisionNotFoundException::new);

        ProvisionResponse response = ProvisionResponse.fromEntity(provision);
        response.setLikesCount(provisionLikeRepository.countByProvisionId(id));
        response.setLikedByMe(provisionLikeRepository.existsByProvisionIdAndUserId(id, userId));

        return response;
    }

    @Transactional
    public ProvisionResponse create(final CreateProvisionRequest dto, final Long userId) {
        User user = userService.findById(userId);
        validateUserRole(user);

        ProvisionCategory provisionCategory = provisionCategoryService.getEntityById(dto.getCategoryId());

        List<ProvisionSlot> slots = dto.getSlots().stream()
                .map(slotDto -> ProvisionSlot.builder()
                        .startTime(slotDto.getStartTime())
                        .endTime(slotDto.getEndTime())
                        .available(true)
                        .build())
                .toList();

        final Provision provision = Provision.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .provisionCategory(provisionCategory)
                .user(user)
                .slots(slots)
                .build();

        slots.forEach(slot -> slot.setProvision(provision));

        return ProvisionResponse.fromEntity(provisionRepository.save(provision));
    }

    @Transactional
    public void toggleLike(final Long provisionId, final Long userId) {
        Provision provision = provisionRepository.findById(provisionId)
                .orElseThrow(ProvisionNotFoundException::new);
        User user = userService.findById(userId);

        provisionLikeRepository.findByProvisionIdAndUserId(provisionId, userId)
                .ifPresentOrElse(
                        provisionLikeRepository::delete,
                        () -> {
                            ProvisionLike like = ProvisionLike.builder()
                                    .provision(provision)
                                    .user(user)
                                    .build();
                            provisionLikeRepository.save(like);
                        }
                );
    }

    @Transactional
    public ProvisionResponse update(final UpdateProvisionRequest dto, final Long userId) {
        Provision existingProvision = provisionRepository.findById(dto.getId())
                .orElseThrow(ProvisionNotFoundException::new);

        if (!existingProvision.getUser().getId().equals(userId)) {
            throw new InsufficientPermissionsToCreateProvisionException();
        }

        if (dto.getTitle() != null) {
            existingProvision.setTitle(dto.getTitle());
        }

        if (dto.getDescription() != null) {
            existingProvision.setDescription(dto.getDescription());
        }

        if (dto.getProvisionCategoryId() != null) {
            ProvisionCategory category = provisionCategoryService.getEntityById(dto.getProvisionCategoryId());
            existingProvision.setProvisionCategory(category);
        }

        Provision updatedProvision = provisionRepository.save(existingProvision);
        return ProvisionResponse.fromEntity(updatedProvision);
    }

    @Transactional
    public void delete(final Long provisionId, final Long userId) {
        Provision provision = provisionRepository.findById(provisionId)
                .orElseThrow(ProvisionNotFoundException::new);

        if (!provision.getUser().getId().equals(userId)) {
            throw new InsufficientPermissionsToDeleteProvisionException();
        }

        provisionRepository.delete(provision);
    }

    @Transactional
    public void removeSlotInProvision(final RemoveProvisionSlotRequest dto, final Long userId) {
        ProvisionSlot slot = provisionSlotRepository.findById(dto.getSlotId())
                .orElseThrow(ProvisionSlotNotFoundException::new);

        Long provisionUserId = provisionRepository.findUserIdById(slot.getProvision().getId());

        if (!Objects.equals(provisionUserId, userId)) {
            throw new InsufficientPermissionsToUpdateProvisionException();
        }

        provisionSlotRepository.delete(slot);
    }

    @Transactional
    public void addSlotToProvision(final CreateProvisionSlotRequest dto, final Long userId) {
        if (dto.getStartTime().isAfter(dto.getEndTime()) ||
                dto.getStartTime().equals(dto.getEndTime())) {
            throw new IncorrectTimeProvisionSlotException();
        }

        Provision provision = provisionRepository.findById(dto.getProvisionId())
                .orElseThrow(ProvisionNotFoundException::new);

        if (!Objects.equals(provision.getUser().getId(), userId)) {
            throw new InsufficientPermissionsToUpdateProvisionException();
        }

        if (provisionSlotRepository.existsOverlappingSlot(
                dto.getProvisionId(),
                dto.getStartTime(),
                dto.getEndTime())) {
            throw new SuchASlotAlreadyExistsException();
        }

        ProvisionSlot slot = ProvisionSlot.builder()
                .provision(provision)
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .available(true)
                .build();

        provisionSlotRepository.save(slot);
    }

    private void validateUserRole(final User user) {
        String role = user.getRole().toString();
        if (!"ADMIN".equals(role) && !"BARBER".equals(role)) {
            throw new InsufficientPermissionsToCreateProvisionException();
        }
    }
}