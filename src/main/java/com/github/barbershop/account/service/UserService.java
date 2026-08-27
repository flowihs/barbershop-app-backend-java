package com.github.barbershop.account.service;

import com.github.barbershop.account.dto.TelegramUserData;
import com.github.barbershop.account.dto.UserDTO;
import com.github.barbershop.account.entity.User;
import com.github.barbershop.account.entity.UserRole;
import com.github.barbershop.account.exception.UserNotFoundException;
import com.github.barbershop.account.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final ConcurrentHashMap<Long, Object> locks = new ConcurrentHashMap<>();

    @Transactional
    public UserDTO verifyTelegram(TelegramUserData telegramUser) {
        Long telegramId = telegramUser.getId();

        Object lock = locks.computeIfAbsent(telegramId, ignored -> new Object());
        synchronized (lock) {
            try {
                User user = userRepository.findByTelegramId(telegramId)
                        .orElseGet(() -> register(telegramUser));
                return UserDTO.fromUser(user);
            } finally {
                locks.remove(telegramId, lock);
            }
        }
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(UserNotFoundException::new);
    }

    public User findByTelegramId(Long telegramId) {
        return userRepository.findByTelegramId(telegramId)
                .orElseThrow(UserNotFoundException::new);
    }

    private User register(TelegramUserData telegramUser) {
        User user = User.builder()
                .telegramId(telegramUser.getId())
                .firstName(telegramUser.getFirstName())
                .lastName(telegramUser.getLastName())
                .username(telegramUser.getUsername())
                .photoUrl(telegramUser.getPhotoUrl())
                .role(UserRole.CLIENT)
                .build();

        return userRepository.save(user);
    }
}
