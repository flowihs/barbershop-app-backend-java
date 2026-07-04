package com.github.barbershop.provision.controller;

import com.github.barbershop.account.entity.User;
import com.github.barbershop.account.security.AuthUtils;
import com.github.barbershop.provision.dto.*;
import com.github.barbershop.provision.service.ProvisionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/provisions")
@RequiredArgsConstructor
@Tag(name = "Управление услугами", description = "API для управления услугами, слотами и лайками")
public class ProvisionController {
    private final ProvisionService provisionService;
    private final AuthUtils authUtils;

    @Operation(summary = "Получить услугу по ID")
    @GetMapping("/{id}")
    public ResponseEntity<ProvisionResponse> getById(@PathVariable Long id) {
        User currentUser = authUtils.getCurrentUser();
        return ResponseEntity.ok(provisionService.getById(id, currentUser.getId()));
    }

    @Operation(summary = "Получить все услуги")
    @GetMapping
    public ResponseEntity<List<ProvisionResponse>> getAll() {
        return ResponseEntity.ok(provisionService.getAll());
    }

    @Operation(summary = "Создать новую услугу (доступно ADMIN и BARBER)")
    @PostMapping("/create")
    public ResponseEntity<ProvisionResponse> create(@RequestBody @Valid CreateProvisionRequest dto) {
        User currentUser = authUtils.getCurrentUser();
        return ResponseEntity.ok(provisionService.create(dto, currentUser.getId()));
    }

    @Operation(summary = "Обновить услугу (только владелец)")
    @PutMapping("/update")
    public ResponseEntity<ProvisionResponse> update(@RequestBody @Valid UpdateProvisionRequest dto) {
        User currentUser = authUtils.getCurrentUser();
        return ResponseEntity.ok(provisionService.update(dto, currentUser.getId()));
    }

    @Operation(summary = "Удалить услугу (только владелец)")
    @DeleteMapping("/delete")
    public ResponseEntity<Void> delete(@RequestParam Long provisionId) {
        User currentUser = authUtils.getCurrentUser();
        provisionService.delete(provisionId, currentUser.getId());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Поставить/убрать лайк")
    @PostMapping("/{id}/like")
    public ResponseEntity<Void> toggleLike(@PathVariable Long id) {
        User currentUser = authUtils.getCurrentUser();
        provisionService.toggleLike(id, currentUser.getId());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Добавить слот к услуге (только владелец)")
    @PostMapping("/add-slot")
    public ResponseEntity<Void> addSlot(@RequestBody @Valid CreateProvisionSlotRequest dto) {
        User currentUser = authUtils.getCurrentUser();
        provisionService.addSlotToProvision(dto, currentUser.getId());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Удалить слот у услуги (только владелец)")
    @PostMapping("/remove-slot")
    public ResponseEntity<Void> removeSlot(@RequestBody @Valid RemoveProvisionSlotRequest dto) {
        User currentUser = authUtils.getCurrentUser();
        provisionService.removeSlotInProvision(dto, currentUser.getId());
        return ResponseEntity.ok().build();
    }
}
