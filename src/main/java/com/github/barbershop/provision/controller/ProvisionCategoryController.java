package com.github.barbershop.provision.controller;

import com.github.barbershop.account.entity.User;
import com.github.barbershop.account.security.AuthUtils;
import com.github.barbershop.provision.dto.CreateProvisionCategoryRequest;
import com.github.barbershop.provision.dto.ProvisionCategoryResponse;
import com.github.barbershop.provision.service.ProvisionCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/provision-categories")
@RequiredArgsConstructor
@Tag(name = "Категории", description = "API для управления категориями услуг")
public class ProvisionCategoryController {
    private final ProvisionCategoryService provisionCategoryService;
    private final AuthUtils authUtils;

    @Operation(
            summary = "Получить все категории",
            description = "Возвращает список всех доступных категорий услуг"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Список категорий успешно получен",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProvisionCategoryResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Внутренняя ошибка сервера"
            )
    })
    @GetMapping
    public List<ProvisionCategoryResponse> getAll() {
        return provisionCategoryService.getAll();
    }

    @PostMapping("/create")
    public ResponseEntity<ProvisionCategoryResponse> create(@RequestBody @Valid CreateProvisionCategoryRequest dto) {
        User currentUser = authUtils.getCurrentUser();
        return ResponseEntity.ok(provisionCategoryService.create(dto, currentUser.getRole()));
    }
}