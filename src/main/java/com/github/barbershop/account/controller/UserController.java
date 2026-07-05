package com.github.barbershop.account.controller;

import com.github.barbershop.account.dto.UserDTO;
import com.github.barbershop.account.entity.User;
import com.github.barbershop.account.dto.TelegramUserData;
import com.github.barbershop.account.service.TelegramAuthValidator;
import com.github.barbershop.account.service.UserService;
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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Аутентификация", description = "API для авторизации через Telegram Mini App")
public class UserController {

    private final TelegramAuthValidator telegramAuthValidator;
    private final UserService userService;

    @Operation(
            summary = "Авторизация через Telegram",
            description = "Авторизует пользователя через данные от Telegram Mini App.\n" +
                    "initData должна быть получена из window.Telegram.WebApp.initData " +
                    "на фронтенде Mini App и передана на бэкенд без изменений."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Успешная авторизация",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Невалидные данные"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Внутренняя ошибка сервера"
            )

    })
    @PostMapping("/auth")
    public ResponseEntity<UserDTO> authenticate(
            @RequestBody(required = false) String rawBody) {

        System.out.println("📥 Raw body: " + rawBody);

        String initData = null;

        if (rawBody == null || rawBody.isEmpty()) {
            throw new RuntimeException("Тело запроса пустое");
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(rawBody);

            if (node.has("initData")) {
                initData = node.get("initData").asText();
            } else if (node.isTextual()) {
                initData = node.asText();
            }
        } catch (Exception e) {
            initData = rawBody;
        }

        if (initData != null && initData.startsWith("\"") && initData.endsWith("\"")) {
            initData = initData.substring(1, initData.length() - 1);
            initData = initData.replace("\\\"", "\"");
        }

        if (initData == null || initData.isEmpty()) {
            throw new RuntimeException("initData не найден или пустой");
        }

        if (!telegramAuthValidator.validate(initData)) {
            throw new RuntimeException("Невалидная сигнатура Telegram");
        }

        TelegramUserData telegramUser = telegramAuthValidator.extractUserData(initData);
        User user = userService.findOrCreateUser(telegramUser);

        return ResponseEntity.ok(UserDTO.fromUser(user));
    }

    @Operation(
            summary = "Получить профиль пользователя",
            description = "Возвращает данные пользователя по его ID в системе",
            tags = {"Пользователи"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Профиль найден",
                    content = @Content(schema = @Schema(implementation = UserDTO.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Пользователь не найден"
            )
    })
    @GetMapping("/users/{userId}")
    public ResponseEntity<UserDTO> getUserProfile(
            @Parameter(description = "ID пользователя", example = "1")
            @PathVariable Long userId) {
        User user = userService.findById(userId);
        return ResponseEntity.ok(UserDTO.fromUser(user));
    }

    @Operation(
            summary = "Получить профиль по Telegram ID",
            description = "Возвращает данные пользователя по его Telegram ID",
            tags = {"Пользователи"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Профиль найден"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Пользователь не найден"
            )
    })
    @GetMapping("/users/telegram/{telegramId}")
    public ResponseEntity<UserDTO> getUserByTelegramId(
            @Parameter(description = "Telegram ID пользователя", example = "123456789")
            @PathVariable Long telegramId) {

        User user = userService.findByTelegramId(telegramId);
        return ResponseEntity.ok(UserDTO.fromUser(user));
    }
}