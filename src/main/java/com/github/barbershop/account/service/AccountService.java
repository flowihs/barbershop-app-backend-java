package com.github.barbershop.account.service;

import com.github.barbershop.account.dto.*;
import com.github.barbershop.account.entity.Account;
import com.github.barbershop.account.entity.UserRole;
import com.github.barbershop.account.exception.AccountNotFoundException;
import com.github.barbershop.account.repository.AccountRepository;
import com.github.barbershop.storage.dto.UploadResult;
import com.github.barbershop.storage.service.StorageService;
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
    private final StorageService storageService;

    @Transactional
    public UserDTO verifyTelegram(TelegramUserData telegramUser) {
        Long telegramId = telegramUser.getId();

        Object lock = locks.computeIfAbsent(telegramId, ignored -> new Object());
        synchronized (lock) {
            try {
                Account user = accountRepository.findByTelegramId(telegramId)
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

    public Account findById(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(AccountNotFoundException::new);
    }

    public Account findByTelegramId(Long telegramId) {
        return accountRepository.findByTelegramId(telegramId)
                .orElseThrow(AccountNotFoundException::new);
    }

    @Transactional
    public UpdateAccountPhotoResponse updateAvatar(UpdatePhotoRequest dto) {
        Account account = accountRepository.findById(dto.getId())
                .orElseThrow(AccountNotFoundException::new);
        UploadResult uploadResult = storageService.uploadImage(dto.getPhoto());
        account.setPhotoUrl(uploadResult.publicUrl());
        accountRepository.save(account);
        return UpdateAccountPhotoResponse.builder()
                .id(account.getId())
                .photoUrl(account.getPhotoUrl())
                .build();
    }

    private Account register(TelegramUserData telegramUser) {
        Account account = Account.builder()
                .telegramId(telegramUser.getId())
                .firstName(telegramUser.getFirstName())
                .lastName(telegramUser.getLastName())
                .username(telegramUser.getUsername())
                .photoUrl(telegramUser.getPhotoUrl())
                .role(UserRole.CLIENT)
                .build();

        return accountRepository.save(account);
    }
}