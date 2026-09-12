package com.github.barbershop.account.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class UpdateAccountPhotoResponse {
    private Long id;
    private String photoUrl;
}
