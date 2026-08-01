package com.github.barbershop.account.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.barbershop.account.dto.TelegramUserData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class TelegramAuthValidator {

    private static final Logger log = LoggerFactory.getLogger(TelegramAuthValidator.class);

    @Value("${telegram.bot.token}")
    private String botToken;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final long MAX_AUTH_AGE_SECONDS = 86400;

    public boolean validate(String initData) {
        try {
            Map<String, String> params = parseQueryString(initData);

            params.remove("signature");

            String receivedHash = params.remove("hash");
            if (receivedHash == null) {
                log.warn("No hash present in initData");
                return false;
            }

            if (!isAuthDateValid(params.get("auth_date"))) {
                log.warn("initData auth_date is missing or expired");
                return false;
            }

            List<String> keys = new ArrayList<>(params.keySet());
            Collections.sort(keys);
            String dataCheckString = keys.stream()
                    .map(k -> k + "=" + params.get(k))
                    .collect(Collectors.joining("\n"));

            log.debug("data_check_string: {}", dataCheckString);

            byte[] secretKey = hmacSha256(
                    "WebAppData".getBytes(StandardCharsets.UTF_8),
                    botToken.getBytes(StandardCharsets.UTF_8)
            );
            byte[] hmac = hmacSha256(secretKey, dataCheckString.getBytes(StandardCharsets.UTF_8));
            String computedHex = bytesToHexLower(hmac);

            boolean isValid = computedHex.equals(receivedHash.toLowerCase());

            if (!isValid) {
                log.warn("Hash mismatch. Computed: {}, Received: {}", computedHex, receivedHash.toLowerCase());
            }

            return isValid;
        } catch (Exception e) {
            log.error("Error validating initData", e);
            return false;
        }
    }

    private boolean isAuthDateValid(String authDate) {
        if (authDate == null || authDate.isBlank()) {
            return false;
        }
        try {
            long authTimestamp = Long.parseLong(authDate);
            long now = System.currentTimeMillis() / 1000;
            return authTimestamp <= now && now - authTimestamp <= MAX_AUTH_AGE_SECONDS;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public TelegramUserData extractUserData(String initData) {
        Map<String, String> params = parseQueryString(initData);
        String userEncoded = params.get("user");
        if (userEncoded == null) {
            throw new IllegalArgumentException("Данных пользователя нет в initData");
        }
        try {
            return objectMapper.readValue(userEncoded, TelegramUserData.class);
        } catch (Exception e) {
            log.error("Failed to parse user JSON", e);
            throw new IllegalArgumentException("Ошибка при парсинге данных пользователя");
        }
    }

    private static Map<String, String> parseQueryString(String qs) {
        Map<String, String> map = new LinkedHashMap<>();
        if (qs == null || qs.isEmpty()) {
            return map;
        }
        if (qs.startsWith("?")) {
            qs = qs.substring(1);
        }
        String[] parts = qs.split("&");
        for (String p : parts) {
            int idx = p.indexOf('=');
            if (idx >= 0) {
                String k = urlDecode(p.substring(0, idx));
                String v = urlDecode(p.substring(idx + 1));
                map.put(k, v);
            } else {
                map.put(urlDecode(p), "");
            }
        }
        return map;
    }

    private static String urlDecode(String s) {
        if (s == null) {
            return null;
        }
        try {
            return URLDecoder.decode(s, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return s;
        }
    }

    private static byte[] hmacSha256(byte[] key, byte[] data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec keySpec = new SecretKeySpec(key, "HmacSHA256");
        mac.init(keySpec);
        return mac.doFinal(data);
    }

    private static String bytesToHexLower(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
