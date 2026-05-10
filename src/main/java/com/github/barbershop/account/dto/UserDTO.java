package com.github.barbershop.account.dto;

import com.github.barbershop.account.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private Long telegramId;
    private String firstName;
    private String lastName;
    private String username;
    private String photoUrl;
    private String role;
    private LocalDateTime createdAt;

    public static UserDTO fromUser(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .telegramId(user.getTelegramId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .username(user.getUsername())
                .photoUrl(user.getPhotoUrl())
                .role(user.getRole().name())
                .createdAt(user.getCreatedAt())
                .build();
    }
}