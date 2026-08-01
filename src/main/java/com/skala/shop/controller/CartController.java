package com.skala.shop.controller;

import com.skala.shop.dto.cart.CartItemRequest;
import com.skala.shop.dto.cart.CartResponse;
import com.skala.shop.dto.cart.CheckoutRequest;
import com.skala.shop.dto.cart.CheckoutResponse;
import com.skala.shop.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cart")
@Tag(name = "장바구니 관리", description = "카트 CRUD API")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    // 카트 조회
    @GetMapping
    @Operation(summary = "카트 조회")
    public ResponseEntity<CartResponse> getCart(Authentication authentication) {
        return ResponseEntity.ok(cartService.getCart(authentication.getName()));
    }

    // 카트에 담기
    @PostMapping
    @Operation(summary = "카트에 담기")
    public ResponseEntity<CartResponse> addToCart(
            Authentication authentication,
            @Valid @RequestBody CartItemRequest request
    ) {
        return ResponseEntity.ok(cartService.addToCart(authentication.getName(), request));
    }

    // 수량 변경
    @PutMapping("/{productId}")
    @Operation(summary = "카트 상품 수량 변경")
    public ResponseEntity<CartResponse> updateQuantity(
            Authentication authentication,
            @PathVariable Long productId,
            @RequestParam int quantity
    ) {
        return ResponseEntity.ok(
                cartService.updateQuantity(authentication.getName(), productId, quantity)
        );
    }

    // 카트에서 특정 상품 삭제
    @DeleteMapping("/{productId}")
    @Operation(summary = "카트에서 상품 삭제")
    public ResponseEntity<CartResponse> removeFromCart(
            Authentication authentication,
            @PathVariable Long productId
    ) {
        return ResponseEntity.ok(
                cartService.removeFromCart(authentication.getName(), productId)
        );
    }

    // 카트 비우기
    @DeleteMapping
    @Operation(summary = "카트 비우기")
    public ResponseEntity<Void> clearCart(Authentication authentication) {
        cartService.clearCart(authentication.getName());
        return ResponseEntity.noContent().build();
    }

    // 카트 선택 주문
    @PostMapping("/checkout")
    @Operation(summary = "카트 선택 주문")
    public ResponseEntity<CheckoutResponse> checkout(
            Authentication authentication,
            @Valid @RequestBody CheckoutRequest request
    ) {
        return ResponseEntity.ok(
                cartService.checkout(authentication.getName(), request)
        );
    }
}
