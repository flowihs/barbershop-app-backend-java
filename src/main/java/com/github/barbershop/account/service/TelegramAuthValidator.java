package com.github.barbershop.account.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.barbershop.account.dto.TelegramUserData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class TelegramAuthValidator {
    private static final String HMAC_SHA_256 = "HmacSHA256";
    private static final byte[] WEB_APP_DATA = "WebAppData".getBytes(StandardCharsets.UTF_8);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.auth.max-age-seconds:86400}")
    private long maxAgeSeconds = 86400;

    public boolean validate(String initData) {
        try {
            Map<String, String> parameters = parse(initData);
            String receivedHash = parameters.remove("hash");

            if (receivedHash == null || !receivedHash.matches("[0-9a-fA-F]{64}")) {
                return false;
            }

            if (!isAuthDateValid(parameters.get("auth_date"))) {
                return false;
            }

            String dataCheckString = parameters.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .map(entry -> entry.getKey() + "=" + entry.getValue())
                    .collect(Collectors.joining("\n"));

            byte[] secretKey = hmac(WEB_APP_DATA, botToken.getBytes(StandardCharsets.UTF_8));
            byte[] calculatedHash = hmac(secretKey, dataCheckString.getBytes(StandardCharsets.UTF_8));
            byte[] suppliedHash = hexToBytes(receivedHash);

            return MessageDigest.isEqual(calculatedHash, suppliedHash);
        } catch (Exception ignored) {
            return false;
        }
    }

    public TelegramUserData validateAndExtract(String initData) {
        if (!validate(initData)) {
            throw new IllegalArgumentException("Invalid or expired Telegram initData");
        }
        return extractUserData(initData);
    }

    public TelegramUserData extractUserData(String initData) {
        try {
            String userJson = parse(initData).get("user");
            if (userJson == null || userJson.isBlank()) {
                throw new IllegalArgumentException("Telegram user data is missing");
            }

            TelegramUserData userData = OBJECT_MAPPER.readValue(userJson, TelegramUserData.class);
            if (userData.getId() == null || userData.getFirstName() == null
                    || userData.getFirstName().isBlank()) {
                throw new IllegalArgumentException("Telegram user data is incomplete");
            }
            return userData;
        } catch (IllegalArgumentException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalArgumentException("Malformed Telegram user data", exception);
        }
    }

    private Map<String, String> parse(String initData) {
        if (initData == null || initData.isBlank()) {
            throw new IllegalArgumentException("Telegram initData is missing");
        }

        Map<String, String> parameters = new LinkedHashMap<>();
        Arrays.stream(initData.split("&"))
                .forEach(pair -> {
                    String[] parts = pair.split("=", 2);
                    if (parts.length != 2) {
                        throw new IllegalArgumentException("Malformed Telegram initData");
                    }

                    String key = URLDecoder.decode(parts[0], StandardCharsets.UTF_8);
                    String value = URLDecoder.decode(parts[1], StandardCharsets.UTF_8);
                    if (key.isBlank() || parameters.putIfAbsent(key, value) != null) {
                        throw new IllegalArgumentException("Duplicate Telegram initData parameter");
                    }
                });
        return parameters;
    }

    private boolean isAuthDateValid(String authDate) {
        if (authDate == null) {
            return false;
        }

        long timestamp = Long.parseLong(authDate);
        long now = Instant.now().getEpochSecond();
        return timestamp <= now + 30 && now - timestamp <= maxAgeSeconds;
    }

    private byte[] hmac(byte[] key, byte[] data) throws Exception {
        Mac mac = Mac.getInstance(HMAC_SHA_256);
        mac.init(new SecretKeySpec(key, HMAC_SHA_256));
        return mac.doFinal(data);
    }

    private byte[] hexToBytes(String hex) {
        byte[] result = new byte[hex.length() / 2];
        for (int i = 0; i < result.length; i++) {
            result[i] = (byte) Integer.parseInt(hex.substring(i * 2, i * 2 + 2), 16);
        }
        return result;
    }
}
