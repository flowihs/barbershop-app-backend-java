package com.github.barbershop.account.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.barbershop.account.dto.UserDTO;
import com.github.barbershop.account.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class TelegramAuthController {
    private final UserService userService;

    @PostMapping("/telegram")
    public ResponseEntity<UserDTO> authTelegram(@RequestBody Map<String, String> body) {
        try {
            String initData = body.get("initData");
            String decodedUserJson = extractUserJson(initData);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(decodedUserJson);

            return ResponseEntity.ok(userService.verifyTelegram(node));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    private String extractUserJson(String initData) {
        if (initData == null) return "{}";

        for (String pair : initData.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2 && kv[0].equals("user")) {
                return URLDecoder.decode(kv[1], StandardCharsets.UTF_8)
                        .replace("\\/", "/");
            }
        }
        return "{}";
    }
}