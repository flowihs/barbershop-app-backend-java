package com.github.barbershop.provision.dto;

import com.github.barbershop.provision.entity.ProvisionCategory;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ProvisionCategoryResponse {
    private Long id;
    private String name;
    private String description;

    public static ProvisionCategoryResponse fromEntity(ProvisionCategory provisionCategory) {
        return ProvisionCategoryResponse.builder()
                .id(provisionCategory.getId())
                .name(provisionCategory.getName())
                .description(provisionCategory.getDescription())
                .build();
    }
}
