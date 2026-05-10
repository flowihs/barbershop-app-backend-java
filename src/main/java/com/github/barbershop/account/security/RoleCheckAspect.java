package com.github.barbershop.account.security;

import com.github.barbershop.account.entity.User;
import com.github.barbershop.account.entity.UserRole;
import com.github.barbershop.account.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;

@Aspect
@Component
@RequiredArgsConstructor
public class RoleCheckAspect {

    private final UserService userService;

    @Before("@annotation(requireRole)")
    public void checkRole(RequireRole requireRole) {
        HttpServletRequest request = ((ServletRequestAttributes)
                RequestContextHolder.currentRequestAttributes()).getRequest();

        String telegramIdHeader = request.getHeader("X-Telegram-Id");
        if (telegramIdHeader == null) {
            throw new RuntimeException("Не авторизован: отсутствует X-Telegram-Id");
        }

        Long telegramId = Long.parseLong(telegramIdHeader);
        User user = userService.findByTelegramId(telegramId);

        String[] requiredRoles = requireRole.value();
        boolean hasRole = Arrays.stream(requiredRoles)
                .anyMatch(role -> user.getRole().name().equals(role));

        if (!hasRole) {
            throw new RuntimeException("Доступ запрещен");
        }
    }
}