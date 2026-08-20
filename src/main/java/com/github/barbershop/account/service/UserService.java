package com.github.barbershop.account.service;

import com.fasterxml.jackson.databind.JsonNode;
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
    public UserDTO verifyTelegram(JsonNode node) {
        Long id = node.get("id").asLong();

        Object lock = locks.computeIfAbsent(id, k -> new Object());
        synchronized (lock) {
            try {
                User user = userRepository.findById(id)
                        .orElseGet(() -> register(node));
                return UserDTO.fromUser(user);
            } finally {
                locks.remove(id);
            }
        }
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(UserNotFoundException::new);
    }

    private User register(JsonNode node) {
        Long id = node.get("id").asLong();
        String firstName = node.get("first_name").asText();
        String lastName = node.get("last_name").asText();
        String username = node.get("username").asText();
        String photoUrl = node.get("photo_url").asText();

        User user = User.builder()
                .id(id)
                .firstName(firstName)
                .lastName(lastName)
                .username(username)
                .photoUrl(photoUrl)
                .role(UserRole.CLIENT)
                .build();

        return userRepository.save(user);
    }
}