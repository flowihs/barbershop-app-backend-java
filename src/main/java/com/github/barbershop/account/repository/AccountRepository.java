package com.github.barbershop.account.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<com.github.barbershop.account.entity.Account, Long> {
    Optional<com.github.barbershop.account.entity.Account> findByTelegramId(Long telegramId);
}
