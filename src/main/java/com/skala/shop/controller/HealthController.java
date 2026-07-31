package com.skala.shop.controller;

import java.util.Map;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.GetMapping;


@RestController
@RequestMapping("/api/health")
@Tag(name = "헬스체크", description = "서버 상태 확인 API")
public class HealthController {

    @GetMapping
    @Operation(summary = "서버 상태 확인")
    public Map<String, String> health() {
        return Map.of("status", "UP", "application", "skala-shop-api");
    }
}
