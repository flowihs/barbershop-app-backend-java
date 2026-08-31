package com.github.barbershop.account.dto;

import com.github.barbershop.account.entity.Account;
import com.github.barbershop.account.entity.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String username;
    private String photoUrl;
    private String description;
    private String tiktok;
    private String instagram;
    private String number;
    private UserRole role;

    public static UserDTO fromUser(Account user) {
        if (user == null) return null;
        return UserDTO.builder()
                .id(user.getTelegramId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .username(user.getUsername())
                .photoUrl(user.getPhotoUrl())
                .role(user.getRole())
                .description(user.getDescription())
                .number(user.getNumber())
                .tiktok(user.getTiktok())
                .instagram(user.getInstagram())
                .build();
    }
}
