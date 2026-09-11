package com.github.barbershop.account.security;

import com.github.barbershop.account.entity.Account;
import com.github.barbershop.account.exception.UnauthorizedException;
import com.github.barbershop.account.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AuthUtils {

    private final AccountService userService;

    public Account getCurrentUser() {
        return getCurrentUserOptional()
                .orElseThrow(UnauthorizedException::new);
    }

    public boolean hasRole(Account account, String role) {
        return account.getRole().name().equals(role);
    }

    public boolean hasAnyRole(Account account, String... roles) {
        for (String role : roles) {
            if (hasRole(account, role)) {
                return true;
            }
        }
        return false;
    }

    public Optional<Account> getCurrentUserOptional() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {
            return Optional.empty();
        }

        try {
            Long telegramId = Long.parseLong(authentication.getName());
            return Optional.of(userService.findByTelegramId(telegramId));
        } catch (NumberFormatException e) {
            throw new RuntimeException("Неверный формат идентификатора пользователя");
        }
    }
}
