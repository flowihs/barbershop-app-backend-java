package com.github.barbershop.provision.controller;

import com.github.barbershop.provision.service.ProvisionReviewService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/provisions")
@RequiredArgsConstructor
@Tag(name = "Управление отзывами", description = "API для управления отзывами")
public class ProvisionReviewController {
    private final ProvisionReviewService provisionReviewService;
}
