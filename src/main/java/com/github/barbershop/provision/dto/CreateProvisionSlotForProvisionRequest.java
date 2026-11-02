package com.github.barbershop.provision.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CreateProvisionSlotForProvisionRequest {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
