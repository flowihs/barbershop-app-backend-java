package com.github.barbershop.provision.controller;

import com.github.barbershop.provision.dto.CreateProvisionRequest;
import com.github.barbershop.provision.dto.ProvisionResponse;
import com.github.barbershop.provision.service.ProvisionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/provisions")
@RequiredArgsConstructor
public class ProvisionController {
    private final ProvisionService provisionService;

    @GetMapping("/{id}")
    public ResponseEntity<ProvisionResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(provisionService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<ProvisionResponse>> getAll() {
        return ResponseEntity.ok(provisionService.getAll());
    }
}