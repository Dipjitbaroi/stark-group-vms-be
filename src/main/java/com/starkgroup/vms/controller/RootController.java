package com.starkgroup.vms.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class RootController {

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> root() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "Stark Group - Vehicle Management System (VMS) API",
                "version", "1.0.0",
                "endpoints", Map.of(
                        "auth", "/api/auth/login",
                        "vehicles", "/api/vehicles"
                )
        ));
    }
}
