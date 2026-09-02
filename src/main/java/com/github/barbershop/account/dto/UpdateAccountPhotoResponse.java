package com.github.barbershop.account.dto;

import lombok.Builder;

@Builder
public class UpdateAccountPhotoResponse {
    private Long id;
    private String photoUrl;
}
