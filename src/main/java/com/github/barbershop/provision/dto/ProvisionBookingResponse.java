package com.github.barbershop.provision.dto;

import com.github.barbershop.provision.entity.ProvisionBooking;
import com.github.barbershop.provision.entity.ProvisionSlot;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Builder
@Getter
public class ProvisionBookingResponse {
    private Long id;
    private Long userId;
    private ProvisionSlot provisionSlot;

    public static ProvisionBookingResponse fromEntity(ProvisionBooking entity) {
        return ProvisionBookingResponse.builder()
                .id(entity.getId())
                .userId(entity.getAccount().getId())
                .provisionSlot(entity.getProvisionSlot())
                .build();
    }
}
