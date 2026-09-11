package com.github.barbershop.provision.dto;

import com.github.barbershop.account.dto.UserDTO;
import com.github.barbershop.provision.entity.Provision;
import com.github.barbershop.provision.entity.ProvisionSlot;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Array;
import java.util.List;

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
                .user(UserDTO.fromUser(provision.getUser()))
                .build();
    }
}
