package com.github.barbershop.provision.dto;

import com.github.barbershop.account.entity.Account;
import com.github.barbershop.provision.entity.ProvisionBooking;
import com.github.barbershop.provision.entity.ProvisionSlot;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Setter
@Getter
public class ProvisionBookingForBarberResponse {
    private Long id;
    private Account account;
    private ProvisionSlot provisionSlot;

    public static ProvisionBookingForBarberResponse fromEntity(ProvisionBooking entity) {
        return ProvisionBookingForBarberResponse.builder()
                .id(entity.getId())
                .account(entity.getAccount())
                .provisionSlot(entity.getProvisionSlot())
                .build();
    }
}
