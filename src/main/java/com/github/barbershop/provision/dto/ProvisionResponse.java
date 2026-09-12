package com.github.barbershop.provision.dto;

import com.github.barbershop.account.dto.UserDTO;
import com.github.barbershop.provision.entity.Provision;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;


@Getter
@Setter
@Builder
public class ProvisionResponse {
    private Long id;
    private String title;
    private String description;
    private float rating;
    private String avatar;
    private long likesCount;
    private boolean isLikedByMe;
    private List<ProvisionSlotResponse>provisionSlots;

    private ProvisionCategoryResponse provisionCategory;
    private UserDTO user;


    public static ProvisionResponse fromEntity(Provision provision) {
        return ProvisionResponse.builder()
                .id(provision.getId())
                .title(provision.getTitle())
                .description(provision.getDescription())
                .rating(provision.getRating())
                .avatar(provision.getAvatar())
                .provisionCategory(ProvisionCategoryResponse.fromEntity(provision.getProvisionCategory()))
                .provisionSlots(provision.getSlots() != null ? provision.getSlots().stream()
                                .map(ProvisionSlotResponse::fromEntity)
                                .collect(Collectors.toList()) : null)
                .user(UserDTO.fromUser(provision.getUser()))
                .build();
    }
}
