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
import java.security.MessageDigest;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class TelegramAuthValidator {

    private static final Logger log = LoggerFactory.getLogger(TelegramAuthValidator.class);

    @Value("${telegram.bot.token}")
    private String botToken;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public boolean validate(String initData) {
        try {
            Map<String, String> params = parseQueryString(initData);
            // Telegram WebApp uses 'hash' field
            String receivedHash = params.remove("hash");
            if (receivedHash == null) {
                receivedHash = params.remove("signature"); // на всякий случай
            }
            if (receivedHash == null) {
                log.warn("No hash/signature present in initData");
                return false;
            }

            // Build data_check_string: sort keys and join "key=value" with URL-decoded values
            List<String> keys = new ArrayList<>(params.keySet());
            Collections.sort(keys);
            String dataCheckString = keys.stream()
                    .map(k -> k + "=" + urlDecode(params.get(k)))
                    .collect(Collectors.joining("\n"));

            log.debug("data_check_string: {}", dataCheckString);

            // secret = SHA256(botToken)
            byte[] secret = sha256(botToken.getBytes(StandardCharsets.UTF_8));

            // compute HMAC-SHA256(secret, data_check_string)
            byte[] hmac = hmacSha256(secret, dataCheckString.getBytes(StandardCharsets.UTF_8));
            String computedHex = bytesToHexLower(hmac);

            log.debug("computed hash: {}, received hash: {}", computedHex, receivedHash.toLowerCase());

            return computedHex.equals(receivedHash.toLowerCase());
        } catch (Exception e) {
            log.error("Error validating initData", e);
            return false;
        }
    }

    public TelegramUserData extractUserData(String initData) {
        Map<String, String> params = parseQueryString(initData);
        String userEncoded = params.get("user");
        if (userEncoded == null) {
            throw new IllegalArgumentException("Данных пользователя нет в initData");
        }
        String userJson = urlDecode(userEncoded);
        try {
            return objectMapper.readValue(userJson, TelegramUserData.class);
        } catch (Exception e) {
            log.error("Failed to parse user JSON", e);
            throw new IllegalArgumentException("Ошибка при парсинге данных пользователя");
        }
    }

    private static Map<String, String> parseQueryString(String qs) {
        Map<String, String> map = new HashMap<>();
        if (qs == null || qs.isEmpty()) return map;
        String[] parts = qs.split("&");
        for (String p : parts) {
            int idx = p.indexOf('=');
            if (idx >= 0) {
                String k = p.substring(0, idx);
                String v = p.substring(idx + 1);
                map.put(k, v);
            } else {
                map.put(p, "");
            }
        }
        return map;
    }

    private static String urlDecode(String s) {
        if (s == null) return null;
        try {
            return URLDecoder.decode(s, StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            return s;
        }
    }

    private static byte[] sha256(byte[] data) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        return md.digest(data);
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
