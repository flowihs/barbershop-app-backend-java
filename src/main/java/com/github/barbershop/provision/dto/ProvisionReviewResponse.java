package com.github.barbershop.provision.dto;

import com.github.barbershop.provision.entity.ProvisionReview;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ProvisionReviewResponse {
    private Long id;
    private String name;
    private String description;
    private float rating;

    public static ProvisionReviewResponse fromEntity(ProvisionReview entity) {
        return ProvisionReviewResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .rating(entity.getRating())
                .build();
    }
}
