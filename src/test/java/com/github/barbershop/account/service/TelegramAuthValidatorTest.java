package com.github.barbershop.account.service;

import com.github.barbershop.account.dto.TelegramUserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class TelegramAuthValidatorTest {

    private static final String BOT_TOKEN = "123456:ABC-DEF";

    private TelegramAuthValidator validator;

    @BeforeEach
    void setUp() {
        validator = new TelegramAuthValidator();
        ReflectionTestUtils.setField(validator, "botToken", BOT_TOKEN);
    }

    @Test
    void validateAcceptsValidInitData() throws Exception {
        String initData = buildInitData(
                "{\"id\":123,\"first_name\":\"Test\",\"username\":\"testuser\"}",
                String.valueOf(Instant.now().getEpochSecond())
        );

        assertTrue(validator.validate(initData));
    }

    @Test
    void validateRejectsTamperedInitData() throws Exception {
        String initData = buildInitData(
                "{\"id\":123,\"first_name\":\"Test\"}",
                String.valueOf(Instant.now().getEpochSecond())
        );

        String tampered = initData.replace("123", "999");

        assertFalse(validator.validate(tampered));
    }

    @Test
    void validateRejectsExpiredInitData() throws Exception {
        String initData = buildInitData(
                "{\"id\":123,\"first_name\":\"Test\"}",
                String.valueOf(Instant.now().minusSeconds(86401).getEpochSecond())
        );

        assertFalse(validator.validate(initData));
    }

    @Test
    void validateRejectsDuplicateParameters() throws Exception {
        String initData = buildInitData(
                "{\"id\":123,\"first_name\":\"Test\"}",
                String.valueOf(Instant.now().getEpochSecond())
        );

        assertFalse(validator.validate(initData + "&auth_date=1"));
    }

    @Test
    void extractUserDataParsesUserJson() throws Exception {
        String initData = buildInitData(
                "{\"id\":123,\"first_name\":\"Test\",\"username\":\"testuser\"}",
                String.valueOf(Instant.now().getEpochSecond())
        );

        TelegramUserData user = validator.extractUserData(initData);

        assertEquals(123L, user.getId());
        assertEquals("Test", user.getFirstName());
        assertEquals("testuser", user.getUsername());
    }

    @Test
    void validateAndExtractRejectsUnsignedData() {
        String unsigned = "user=%7B%22id%22%3A123%2C%22first_name%22%3A%22Test%22%7D"
                + "&auth_date=" + Instant.now().getEpochSecond();

        assertThrows(IllegalArgumentException.class, () -> validator.validateAndExtract(unsigned));
    }

    private String buildInitData(String userJson, String authDate) throws Exception {
        String dataCheckString = "auth_date=" + authDate + "\nuser=" + userJson;

        Mac secretKeyMac = Mac.getInstance("HmacSHA256");
        secretKeyMac.init(new SecretKeySpec("WebAppData".getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] secretKey = secretKeyMac.doFinal(BOT_TOKEN.getBytes(StandardCharsets.UTF_8));

        Mac hashMac = Mac.getInstance("HmacSHA256");
        hashMac.init(new SecretKeySpec(secretKey, "HmacSHA256"));
        byte[] hashBytes = hashMac.doFinal(dataCheckString.getBytes(StandardCharsets.UTF_8));
        String hash = bytesToHex(hashBytes);

        return "user=" + URLEncoder.encode(userJson, StandardCharsets.UTF_8)
                + "&auth_date=" + authDate
                + "&hash=" + hash;
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
