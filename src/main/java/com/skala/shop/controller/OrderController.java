package com.skala.shop.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.skala.shop.dto.order.CustomerOrderResponse;
import com.skala.shop.dto.order.OrderRequest;
import com.skala.shop.dto.order.OrderResultResponse;
import com.skala.shop.service.OrderService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/customers")
@Tag(name = "주문 관리", description = "내 주문 조회, 주문, 취소 API")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // 내 주문 조회
    @GetMapping("/me")
    @Operation(summary = "내 주문 조회")
    public ResponseEntity<CustomerOrderResponse> findMyOrders(Authentication authentication) {
        return ResponseEntity.ok(orderService.findMyOrders(authentication.getName()));
    }

    // 상품 주문
    @PostMapping("/order")
    @Operation(summary = "상품 주문")
    public ResponseEntity<OrderResultResponse> placeOrder(
            Authentication authentication,
            @Valid @RequestBody OrderRequest request) {
        return ResponseEntity.ok(
                orderService.placeOrder(authentication.getName(), request));
    }

    // 주문 취소
    @PostMapping("/cancel")
    @Operation(summary = "주문 취소")
    public ResponseEntity<OrderResultResponse> cancelOrder(
            Authentication authentication,
            @Valid @RequestBody OrderRequest request) {
        return ResponseEntity.ok(
                orderService.cancelOrder(authentication.getName(), request));
    }
}
