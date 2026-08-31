package com.github.barbershop.account.service;

import com.github.barbershop.account.dto.*;
import com.github.barbershop.account.entity.Account;
import com.github.barbershop.account.entity.UserRole;
import com.github.barbershop.account.exception.AccountNotFoundException;
import com.github.barbershop.account.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final ConcurrentHashMap<Long, Object> locks = new ConcurrentHashMap<>();

    @Transactional
    public UserDTO verifyTelegram(TelegramUserData telegramUser) {
        Long telegramId = telegramUser.getId();

        Object lock = locks.computeIfAbsent(telegramId, ignored -> new Object());
        synchronized (lock) {
            try {
                com.github.barbershop.account.entity.Account user = accountRepository.findByTelegramId(telegramId)
                        .orElseGet(() -> register(telegramUser));
                return UserDTO.fromUser(user);
            } finally {
                locks.remove(telegramId, lock);
            }
        }
    }

    public UserDTO getProfile(Long id) {
        return UserDTO.fromUser(accountRepository.findById(id)
                .orElseThrow(AccountNotFoundException::new));
    }

    @Transactional
    public void changeDescriptionAccount(UpdateDescriptionAccountRequest dto) {
        Account account = accountRepository.findById(dto.getId())
                .orElseThrow(AccountNotFoundException::new);

        if (!dto.getDescription().isEmpty()) {
            account.setDescription(dto.getDescription());
        }

        accountRepository.save(account);
    }

    @Transactional
    public void changeSocialNetworksAccount(UpdateAccountSocialNetworksRequest dto) {
        Account account = accountRepository.findById(dto.getId())
                .orElseThrow(AccountNotFoundException::new);

        if (!dto.getInstagram().isEmpty()) {
            account.setInstagram(dto.getInstagram());
        }

        if (!dto.getNumber().isEmpty()) {
            account.setNumber(dto.getNumber());
        }

        if (!dto.getTiktok().isEmpty()) {
            account.setTiktok(dto.getTiktok());
        }

        accountRepository.save(account);
    }

    public com.github.barbershop.account.entity.Account findById(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(AccountNotFoundException::new);
    }

    public com.github.barbershop.account.entity.Account findByTelegramId(Long telegramId) {
        return accountRepository.findByTelegramId(telegramId)
                .orElseThrow(AccountNotFoundException::new);
    }

    @Transactional
    public void updateAvatar(UpdatePhotoRequest dto) {
        Account account = accountRepository.findById(dto.getId())
                .orElseThrow(AccountNotFoundException::new);

        try {
            String photoUrl = uploadFile(dto.getPhoto()); // логика загрузки
            account.setPhotoUrl(photoUrl);
            accountRepository.save(account);
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload avatar");
        }
    }

    private String uploadFile(MultipartFile file) throws IOException {
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path path = Paths.get("uploads/avatars/" + fileName);
        Files.createDirectories(path.getParent());
        Files.write(path, file.getBytes());
        return "/uploads/avatars/" + fileName;
    }

    private com.github.barbershop.account.entity.Account register(TelegramUserData telegramUser) {
        com.github.barbershop.account.entity.Account user = com.github.barbershop.account.entity.Account.builder()
                .telegramId(telegramUser.getId())
                .firstName(telegramUser.getFirstName())
                .lastName(telegramUser.getLastName())
                .username(telegramUser.getUsername())
                .photoUrl(telegramUser.getPhotoUrl())
                .role(UserRole.CLIENT)
                .build();

        return accountRepository.save(user);
    }
}
