package com.github.barbershop.provision.dto;

import com.github.barbershop.account.dto.UserDTO;
import com.github.barbershop.account.entity.User;
import com.github.barbershop.provision.entity.Provision;
import com.github.barbershop.provision.entity.ProvisionCategory;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ProvisionResponse {
    private Long id;
    private String title;
    private String description;
    private float rating;
    private ProvisionCategoryResponse provisionCategory;
    private UserDTO user;

    private long likesCount;
    private boolean isLikedByMe;

    public static ProvisionResponse fromEntity(Provision provision) {
        return ProvisionResponse.builder()
                .id(provision.getId())
                .title(provision.getTitle())
                .description(provision.getDescription())
                .rating(provision.getRating())
                .provisionCategory(ProvisionCategoryResponse.fromEntity(provision.getProvisionCategory()))
                .user(UserDTO.fromUser(provision.getUser()))
                .build();
    }
}
