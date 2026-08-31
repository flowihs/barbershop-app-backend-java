package com.github.barbershop.account.controller;

import com.github.barbershop.account.dto.UpdateAccountSocialNetworksRequest;
import com.github.barbershop.account.dto.UpdateDescriptionAccountRequest;
import com.github.barbershop.account.dto.UpdatePhotoRequest;
import com.github.barbershop.account.dto.UserDTO;
import com.github.barbershop.account.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/account")
public class AccountController {
    private final AccountService accountService;

    @PostMapping("/update-social-netoworks")
    public void updateSocialNetworks (@RequestBody @Valid final UpdateAccountSocialNetworksRequest dto) {
        accountService.changeSocialNetworksAccount(dto);
    }

    @PostMapping("/update-description")
    public void updateDescription (@RequestBody @Valid final UpdateDescriptionAccountRequest dto) {
        accountService.changeDescriptionAccount(dto);
    }

    @GetMapping("/profile/{id}")
    public ResponseEntity<UserDTO> getProfile (@RequestParam("id") Long id) {
        return ResponseEntity.ok(accountService.getProfile(id));
    }

    @PostMapping("/update-avatar")
    public ResponseEntity<Void> updateAvatar(@RequestBody UpdatePhotoRequest dto) {
        accountService.updateAvatar(dto);
        return ResponseEntity.ok().build();
    }
}
