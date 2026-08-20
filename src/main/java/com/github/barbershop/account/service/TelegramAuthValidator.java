package com.github.barbershop.account.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TelegramAuthValidator {
    @Value("${telegram.bot.token}")
    private String botToken;

}
