package com.github.barbershop.provision.dto;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CreateProvisionSlotRequest {
    private Long provisionId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
