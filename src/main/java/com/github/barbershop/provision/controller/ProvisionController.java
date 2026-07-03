package com.github.barbershop.provision.controller;

import com.github.barbershop.account.entity.User;
import com.github.barbershop.account.security.AuthUtils;
import com.github.barbershop.provision.dto.CreateProvisionRequest;
import com.github.barbershop.provision.dto.ProvisionResponse;
import com.github.barbershop.provision.service.ProvisionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/provisions")
@RequiredArgsConstructor
@Tag(name = "Материалы и расходники", description = "API для управления материалами и расходниками")
public class ProvisionController {
    private final ProvisionService provisionService;
    private final AuthUtils authUtils;

    @Operation(
            summary = "Получить материал по ID",
            description = "Возвращает данные материала/расходника по его ID"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Материал найден",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProvisionResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Материал не найден"
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProvisionResponse> getById(
            @PathVariable Long id) {
        User currentUser = authUtils.getCurrentUser();
        return ResponseEntity.ok(provisionService.getById(id, currentUser.getId()));
    }

    @Operation(
            summary = "Получить все материалы",
            description = "Возвращает список всех доступных материалов и расходников"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Список материалов успешно получен",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProvisionResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Внутренняя ошибка сервера"
            )
    })
    @GetMapping
    public ResponseEntity<List<ProvisionResponse>> getAll() {
        return ResponseEntity.ok(provisionService.getAll());
    }

    @PostMapping("/create")
    public ResponseEntity<ProvisionResponse> create(CreateProvisionRequest dto) {
        User currentUser = authUtils.getCurrentUser();
        return ResponseEntity.ok(provisionService.create(dto, currentUser.getId()));
    }

    @Operation(summary = "Поставить/убрать лайк")
    @PostMapping("/{id}/like")
    public ResponseEntity<Void> toggleLike(@PathVariable Long id) {
        User currentUser = authUtils.getCurrentUser();
        provisionService.toggleLike(id, currentUser.getId());
        return ResponseEntity.ok().build();
    }
}