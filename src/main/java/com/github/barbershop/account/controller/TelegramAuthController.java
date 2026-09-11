package com.github.barbershop.account.controller;

import com.github.barbershop.account.dto.AuthResponse;
import com.github.barbershop.account.dto.TelegramAuthRequest;
import com.github.barbershop.account.dto.TelegramUserData;
import com.github.barbershop.account.dto.UserDTO;
import com.github.barbershop.account.entity.Account;
import com.github.barbershop.account.security.AuthUtils;
import com.github.barbershop.account.security.JwtTokenProvider;
import com.github.barbershop.account.service.AccountService;
import com.github.barbershop.account.service.TelegramAuthValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Tag(name = "Авторизация", description = "Telegram Mini App авторизация через initData и JWT")
public class TelegramAuthController {
    private final AccountService userService;
    private final TelegramAuthValidator telegramAuthValidator;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthUtils authUtils;

    @Operation(summary = "Вход через Telegram initData",
            description = "Первый вход или обновление сессии. Передай initData из Telegram.WebApp.initData")
    @PostMapping("/telegram")
    public ResponseEntity<AuthResponse> authTelegram(@Valid @RequestBody TelegramAuthRequest request) {
        try {
            TelegramUserData telegramUser = telegramAuthValidator.validateAndExtract(request.getInitData());
            UserDTO user = userService.verifyTelegram(telegramUser);
            String token = jwtTokenProvider.generateToken(user.getId());

            return ResponseEntity.ok(AuthResponse.builder()
                    .token(token)
                    .user(user)
                    .build());
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @Operation(summary = "Текущий пользователь",
            description = "Проверка JWT-сессии. Используй для автоматического входа при повторном открытии приложения")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser() {
        Account account = authUtils.getCurrentUser();
        return ResponseEntity.ok(UserDTO.fromUser(account));
    }
}
