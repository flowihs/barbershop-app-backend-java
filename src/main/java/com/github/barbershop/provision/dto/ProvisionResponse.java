package com.github.barbershop.provision.dto;

import com.github.barbershop.category.entity.Category;
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
    private Category category;
}
