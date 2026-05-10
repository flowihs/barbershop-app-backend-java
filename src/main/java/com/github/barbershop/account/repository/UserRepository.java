package com.github.barbershop.account.repository;

import com.github.barbershop.account.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByTelegramId(Long telegramId);
    Optional<User> findByUsername(String username);
    boolean existsByTelegramId(Long telegramId);
}