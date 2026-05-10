package com.github.barbershop.account.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TelegramAuthRequest {
    private String initData;
    private String authType;
    private String referralCode;
    private String languageCode;
}
