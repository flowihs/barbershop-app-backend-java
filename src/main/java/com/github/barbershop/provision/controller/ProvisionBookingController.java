package com.github.barbershop.provision.controller;

import com.github.barbershop.account.entity.Account;
import com.github.barbershop.account.security.AuthUtils;
import com.github.barbershop.provision.dto.BookingSlotRequest;
import com.github.barbershop.provision.service.ProvisionBookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/provision-booking")
@RequiredArgsConstructor
@Tag(name = "Управление услугами", description = "API для управления услугами, слотами и лайками")
public class ProvisionBookingController {
    private final ProvisionBookingService provisionBookingService;
    private final AuthUtils authUtils;

    @Operation(summary = "Получить услугу по ID (доступно всем)")
    @PostMapping("/{id}")
    public ResponseEntity<Void> bookingSlot(@RequestBody BookingSlotRequest dto) {
        Long currentUserId = authUtils.getCurrentUserOptional()
                .map(Account::getId)
                .orElse(null);
        provisionBookingService.bookingSlot(dto, currentUserId);
        return ResponseEntity.ok().build();
    }
}
