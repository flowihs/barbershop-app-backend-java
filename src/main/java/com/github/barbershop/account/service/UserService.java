package com.github.barbershop.account.service;

import com.github.barbershop.account.exception.UserNotFoundException;
import com.github.barbershop.account.dto.TelegramUserData;
import com.github.barbershop.account.entity.User;
import com.github.barbershop.account.entity.UserRole;
import com.github.barbershop.account.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public User findOrCreateUser(TelegramUserData telegramUser) {
        return userRepository.findByTelegramId(telegramUser.getId())
                .orElseGet(() -> createNewUser(telegramUser));
    }

    private User createNewUser(TelegramUserData telegramUser) {
        User newUser = User.builder()
                .telegramId(telegramUser.getId())
                .firstName(telegramUser.getFirstName())
                .lastName(telegramUser.getLastName())
                .username(telegramUser.getUsername())
                .photoUrl(telegramUser.getPhotoUrl())
                .role(UserRole.CLIENT)
                .build();
        return userRepository.save(newUser);
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(UserNotFoundException::new);
    }

    public User findByTelegramId(Long telegramId) {
        return userRepository.findByTelegramId(telegramId)
                .orElseThrow(UserNotFoundException::new);
    }
}