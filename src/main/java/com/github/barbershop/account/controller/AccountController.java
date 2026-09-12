package com.github.barbershop.account.controller;

import com.github.barbershop.account.dto.*;
import com.github.barbershop.account.security.AuthUtils;
import com.github.barbershop.account.security.RequireRole;
import com.github.barbershop.account.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/account")
public class AccountController {
    private final AccountService accountService;
    private final AuthUtils authUtils;

    @PostMapping("/update-social-netoworks")
    public void updateSocialNetworks (@RequestBody @Valid final UpdateAccountSocialNetworksRequest dto) {
        accountService.changeSocialNetworksAccount(dto);
    }

    @PostMapping("/update-description")
    public void updateDescription (@RequestBody @Valid final UpdateDescriptionAccountRequest dto) {
        accountService.changeDescriptionAccount(dto);
    }

    @RequireRole("ADMIN")
    @GetMapping("/profile/{id}")
    public ResponseEntity<UserDTO> getProfile(@PathVariable("id") Long id) {
        return ResponseEntity.ok(accountService.getProfile(id));
    }

    @PostMapping(value = "/update-avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UpdateAccountPhotoResponse> updateAvatar(
            @ModelAttribute UpdatePhotoRequest dto) {
        return ResponseEntity.ok(accountService.updateAvatar(dto, authUtils.getCurrentUser()));
    }
}
