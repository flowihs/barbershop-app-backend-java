package com.github.barbershop.account.controller;

import com.github.barbershop.account.dto.AuthResponse;
import com.github.barbershop.account.dto.TelegramAuthRequest;
import com.github.barbershop.account.dto.TelegramUserData;
import com.github.barbershop.account.dto.UserDTO;
import com.github.barbershop.account.security.JwtTokenProvider;
import com.github.barbershop.account.service.TelegramAuthValidator;
import com.github.barbershop.account.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class TelegramAuthController {
    private final UserService userService;
    private final TelegramAuthValidator telegramAuthValidator;
    private final JwtTokenProvider jwtTokenProvider;

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
}
