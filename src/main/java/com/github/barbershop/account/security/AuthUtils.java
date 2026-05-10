package com.github.barbershop.account.security;

import com.github.barbershop.account.entity.User;
import com.github.barbershop.account.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
@RequiredArgsConstructor
public class AuthUtils {

    private final UserService userService;

    public User getCurrentUser() {
        HttpServletRequest request = ((ServletRequestAttributes)
                RequestContextHolder.currentRequestAttributes()).getRequest();

        String telegramIdHeader = request.getHeader("X-Telegram-Id");
        if (telegramIdHeader == null) {
            throw new RuntimeException("Пользователь не авторизован");
        }

        return userService.findByTelegramId(Long.parseLong(telegramIdHeader));
    }
}