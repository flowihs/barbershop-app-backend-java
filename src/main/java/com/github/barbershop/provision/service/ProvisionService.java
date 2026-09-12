package com.github.barbershop.provision.service;

import com.github.barbershop.account.entity.Account;
import com.github.barbershop.account.entity.UserRole;
import com.github.barbershop.account.service.AccountService;
import com.github.barbershop.provision.dto.*;
import com.github.barbershop.provision.entity.Provision;
import com.github.barbershop.provision.entity.ProvisionCategory;
import com.github.barbershop.provision.entity.ProvisionLike;
import com.github.barbershop.provision.entity.ProvisionSlot;
import com.github.barbershop.provision.exception.*;
import com.github.barbershop.provision.repository.ProvisionLikeRepository;
import com.github.barbershop.provision.repository.ProvisionRepository;
import com.github.barbershop.provision.repository.ProvisionSlotRepository;
import com.github.barbershop.storage.dto.UploadResult;
import com.github.barbershop.storage.service.StorageService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ProvisionService {
    private final ProvisionRepository provisionRepository;
    private final ProvisionCategoryService provisionCategoryService;
    private final AccountService userService;
    private final ProvisionLikeRepository provisionLikeRepository;
    private final ProvisionSlotRepository provisionSlotRepository;
    private final StorageService storageService;

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
        response.setLikedByMe(userId != null
                && provisionLikeRepository.existsByProvisionIdAndUserId(id, userId));

        return response;
    }

    public List<ProvisionResponse> getTop5ByUserIdOrderByRatingDesc(final Long id) {
        return provisionRepository.findTop5ByUserIdOrderByRatingDesc(id).stream()
                .map(ProvisionResponse::fromEntity)
                .toList();
    }

    @Transactional
    public ProvisionResponse create(final CreateProvisionRequest dto, final Long userId) {
        Account user = userService.findById(userId);
        validateUserRole(user);

        ProvisionCategory provisionCategory = provisionCategoryService.getEntityById(dto.getCategoryId());

        List<ProvisionSlot> slots = dto.getSlots().stream()
                .map(slotDto -> ProvisionSlot.builder()
                        .startTime(slotDto.getStartTime())
                        .endTime(slotDto.getEndTime())
                        .available(true)
                        .build())
                .toList();

        List<UploadResult> uploadResults = storageService.uploadImages(dto.getImages());

        final Provision provision = Provision.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .provisionCategory(provisionCategory)
                .user(user)
                .slots(slots)
                .avatar(dto.getAvatar())
                .images(uploadResults.stream().map(UploadResult::publicUrl).toList())
                .build();

        slots.forEach(slot -> slot.setProvision(provision));

        return ProvisionResponse.fromEntity(provisionRepository.save(provision));
    }

    @Transactional
    public void toggleLike(final Long provisionId, final Long userId) {
        Optional<ProvisionLike> existingLike = provisionLikeRepository
                .findByProvisionIdAndUserId(provisionId, userId);

        if (existingLike.isPresent()) {
            provisionLikeRepository.delete(existingLike.get());
            provisionLikeRepository.flush();
        } else {
            Provision provision = provisionRepository.findById(provisionId)
                    .orElseThrow(ProvisionNotFoundException::new);
            Account user = userService.findById(userId);

            ProvisionLike like = ProvisionLike.builder()
                    .provision(provision)
                    .user(user)
                    .build();
            provisionLikeRepository.save(like);
        }
    }
    @Transactional
    public ProvisionResponse update(final UpdateProvisionRequest dto, final Long userId) {
        Provision existingProvision = provisionRepository.findById(dto.getId())
                .orElseThrow(ProvisionNotFoundException::new);

        if (!canManage(existingProvision, userId)) {
            throw new InsufficientPermissionsToUpdateProvisionException();
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

        if (dto.getAvatar() != null) {
            existingProvision.setAvatar(dto.getAvatar());
        }

        if (dto.getImages() != null) {
            List<UploadResult> uploadResult = storageService.uploadImages(dto.getImages());
            existingProvision.setImages(uploadResult.stream().map(UploadResult::publicUrl).toList());
        }

        Provision updatedProvision = provisionRepository.save(existingProvision);
        return ProvisionResponse.fromEntity(updatedProvision);
    }

    @Transactional
    public ProvisionResponse updateImages(final Long provisionId,
                                          final List<MultipartFile> files,
                                          final Long userId) {
        Provision provision = provisionRepository.findById(provisionId)
                .orElseThrow(ProvisionNotFoundException::new);

        if (!canManage(provision, userId)) {
            throw new InsufficientPermissionsToUpdateProvisionException();
        }

        List<String> imageUrls = storageService.uploadImages(files).stream()
                .map(UploadResult::publicUrl)
                .toList();
        provision.setImages(imageUrls);

        return ProvisionResponse.fromEntity(provisionRepository.save(provision));
    }

    @Transactional
    public void delete(final Long provisionId, final Long userId) {
        Provision provision = provisionRepository.findById(provisionId)
                .orElseThrow(ProvisionNotFoundException::new);

        if (!canManage(provision, userId)) {
            throw new InsufficientPermissionsToDeleteProvisionException();
        }

        provisionRepository.delete(provision);
    }

    @Transactional
    public void removeSlotInProvision(final RemoveProvisionSlotRequest dto, final Long userId) {
        ProvisionSlot slot = provisionSlotRepository.findById(dto.getSlotId())
                .orElseThrow(ProvisionSlotNotFoundException::new);

        Long provisionUserId = provisionRepository.findUserIdById(slot.getProvision().getId());

        if (!Objects.equals(provisionUserId, userId) && !isAdmin(userId)) {
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

        if (!canManage(provision, userId)) {
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

    private void validateUserRole(final Account user) {
        if (user.getRole() != UserRole.ADMIN && user.getRole() != UserRole.BARBER) {
            throw new InsufficientPermissionsToCreateProvisionException();
        }
    }

    private boolean canManage(Provision provision, Long userId) {
        return Objects.equals(provision.getUser().getId(), userId) || isAdmin(userId);
    }

    private boolean isAdmin(Long userId) {
        return userService.findById(userId).getRole() == UserRole.ADMIN;
    }
}
