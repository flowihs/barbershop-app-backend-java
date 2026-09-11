package com.github.barbershop.account.security;

import com.github.barbershop.account.entity.Account;
import com.github.barbershop.account.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final AccountRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String telegramId) throws UsernameNotFoundException {
        final Long parsedTelegramId;
        try {
            parsedTelegramId = Long.parseLong(telegramId);
        } catch (NumberFormatException exception) {
            throw new UsernameNotFoundException("Invalid Telegram user id", exception);
        }

        Account user = userRepository.findByTelegramId(parsedTelegramId)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with Telegram id: " + telegramId));

        return new org.springframework.security.core.userdetails.User(
                user.getTelegramId().toString(),
                "",
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }
}
