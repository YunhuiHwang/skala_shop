package com.skala.shop.dto.cart;

import com.skala.shop.domain.cart.CartItem;

public record CartItemResponse(
        Long productId,
        String productName,
        long unitPrice,
        int quantity,
        long subtotalPrice
        ) {

    public static CartItemResponse from(CartItem cartItem) {

        long subtotalPrice = Math.multiplyExact(
                cartItem.getProduct().getPrice(),
                (long) cartItem.getQuantity()
        );

        return new CartItemResponse(
                cartItem.getProduct().getId(),
                cartItem.getProduct().getName(),
                cartItem.getProduct().getPrice(),
                cartItem.getQuantity(),
                subtotalPrice
        );
    }
}
