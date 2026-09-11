package com.github.barbershop.account.security;

import com.github.barbershop.account.entity.Account;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
@RequiredArgsConstructor
public class RoleCheckAspect {

    private final AuthUtils authUtils;

    @Before("@annotation(requireRole)")
    public void checkRole(RequireRole requireRole) {
        Account user = authUtils.getCurrentUser();

        boolean hasRole = Arrays.stream(requireRole.value())
                .anyMatch(role -> user.getRole().name().equals(role));

        if (!hasRole) {
            throw new AccessDeniedException("Доступ запрещен");
        }
    }
}
