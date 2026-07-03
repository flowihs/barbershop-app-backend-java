package com.github.barbershop.account.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.barbershop.account.dto.TelegramUserData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class TelegramAuthValidator {

    @Value("${telegram.bot.token}")
    private String botToken;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public boolean validate(String initData) {
        try {
            Map<String, String> params = parseQueryString(initData);
            String hash = params.remove("hash");

            if (hash == null) {
                return false;
            }

            String dataCheckString = params.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .map(e -> e.getKey() + "=" + e.getValue())
                    .collect(Collectors.joining("\n"));

            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(
                    "WebAppData".getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            );
            mac.init(secretKey);
            byte[] secretKeyBytes = mac.doFinal(botToken.getBytes(StandardCharsets.UTF_8));

            SecretKeySpec finalKey = new SecretKeySpec(secretKeyBytes, "HmacSHA256");
            mac.init(finalKey);
            byte[] calculatedHash = mac.doFinal(dataCheckString.getBytes(StandardCharsets.UTF_8));

            String calculatedHex = bytesToHex(calculatedHash);

            return calculatedHex.equals(hash);
        } catch (Exception e) {
            return false;
        }
    }

    private Map<String, String> parseQueryString(String queryString) {
        Map<String, String> params = new HashMap<>();

        if (queryString == null || queryString.isEmpty()) {
            return params;
        }

        String[] pairs = queryString.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=", 2);
            if (keyValue.length == 2) {
                String key = URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8);
                String value = URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);
                params.put(key, value);
            }
        }

        return params;
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public TelegramUserData extractUserData(String initData) {
        Map<String, String> params = parseQueryString(initData);
        String userJson = params.get("user");

        if (userJson == null) {
            throw new IllegalArgumentException("Данных пользователя нет в initData");
        }

        return parseUserJson(userJson);
    }

    private TelegramUserData parseUserJson(String userJson) {
        try {
            return objectMapper.readValue(userJson, TelegramUserData.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Ошибка при парсинге данных пользователя");
        }
    }
}