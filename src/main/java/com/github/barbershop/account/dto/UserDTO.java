package com.github.barbershop.account.dto;

import com.github.barbershop.account.entity.User;
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
    private UserRole role;

    public static UserDTO fromUser(User user) {
        if (user == null) return null;
        return UserDTO.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .username(user.getUsername())
                .photoUrl(user.getPhotoUrl())
                .role(user.getRole())
                .build();
    }
}