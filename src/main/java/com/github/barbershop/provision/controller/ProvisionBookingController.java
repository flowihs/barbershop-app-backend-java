package com.github.barbershop.provision.controller;

import com.github.barbershop.account.entity.Account;
import com.github.barbershop.account.security.AuthUtils;
import com.github.barbershop.account.security.RequireRole;
import com.github.barbershop.provision.dto.BookingSlotRequest;
import com.github.barbershop.provision.dto.ProvisionBookingForBarberResponse;
import com.github.barbershop.provision.dto.ProvisionBookingResponse;
import com.github.barbershop.provision.service.ProvisionBookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/provision-booking")
@RequiredArgsConstructor
@Tag(name = "Управление услугами", description = "API для управления услугами, слотами и лайками")
public class ProvisionBookingController {
    private final ProvisionBookingService provisionBookingService;
    private final AuthUtils authUtils;

    @Operation(summary = "Забронировать услугу на определенное время")
    @PostMapping()
    public ResponseEntity<Void> bookingSlot(@RequestBody BookingSlotRequest dto) {
        Long currentUserId = authUtils.getCurrentUserOptional()
                .map(Account::getId)
                .orElse(null);
        provisionBookingService.bookingSlot(dto, currentUserId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Отмена брони барбером")
    @PostMapping("/cancel/{id}")
    @RequireRole("BARBER")
    public ResponseEntity<Void> cancelBookingForBarber(@PathVariable Long id) {
        provisionBookingService.cancelBookingForBarber(id, authUtils.getCurrentUser().getId());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Получить брони конкретного пользователя")
    @GetMapping("/{userId}")
    public ResponseEntity<List<ProvisionBookingResponse>> getBookingsForClient(@PathVariable Long id) {
        return ResponseEntity.ok(provisionBookingService.getBookingForClient(id));
    }

    @Operation(summary = "Получение брони для барбера")
    @GetMapping("/{userId}")
    public ResponseEntity<List<ProvisionBookingForBarberResponse>> getBookingForBarber(@PathVariable Long id) {
        return ResponseEntity.ok(provisionBookingService.getBookingForBarber(id));
    }
}
