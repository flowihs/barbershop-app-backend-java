package com.github.barbershop.account.dto;

import lombok.Getter;

@Getter
public class UpdateAccountSocialNetworksRequest {
    private Long id;
    private String tiktok;
    private String instagram;
    private String number;
}
