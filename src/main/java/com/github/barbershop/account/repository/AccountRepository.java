package com.github.barbershop.account.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<com.github.barbershop.account.entity.Account, Long> {
    Optional<com.github.barbershop.account.entity.Account> findByTelegramId(Long telegramId);
}
