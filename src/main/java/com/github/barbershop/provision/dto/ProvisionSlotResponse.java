package com.github.barbershop.provision.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.github.barbershop.provision.entity.ProvisionSlot;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter 
@Getter 
@Builder 
public class ProvisionSlotResponse {
    private Long id;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endTime;

    private boolean available;

    public static ProvisionSlotResponse fromEntity(ProvisionSlot entity) {
        return ProvisionSlotResponse.builder()
        .id(entity.getId())
        .startTime(entity.getStartTime())
        .endTime(entity.getEndTime())
        .available(entity.isAvailable())
        .build();
    }
}
