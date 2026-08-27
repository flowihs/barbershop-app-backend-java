package com.github.barbershop.account.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtTokenProviderTest {
    private JwtTokenProvider tokenProvider;

    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(
                tokenProvider,
                "jwtSecret",
                "test-jwt-secret-that-is-at-least-thirty-two-bytes-long"
        );
        ReflectionTestUtils.setField(tokenProvider, "jwtExpiration", 3600000L);
    }

    @Test
    void generatedTokenContainsTelegramIdAndIsValid() {
        String token = tokenProvider.generateToken(1234567890123L);

        assertTrue(tokenProvider.validateToken(token));
        assertEquals(1234567890123L, tokenProvider.getTelegramIdFromToken(token));
    }

    @Test
    void validateRejectsTamperedToken() {
        String token = tokenProvider.generateToken(123L);
        char replacement = token.charAt(token.length() - 1) == 'a' ? 'b' : 'a';
        String tampered = token.substring(0, token.length() - 1) + replacement;

        assertFalse(tokenProvider.validateToken(tampered));
    }

    @Test
    void validateRejectsExpiredToken() {
        ReflectionTestUtils.setField(tokenProvider, "jwtExpiration", -1000L);

        assertFalse(tokenProvider.validateToken(tokenProvider.generateToken(123L)));
    }
}
