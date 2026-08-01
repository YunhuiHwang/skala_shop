package com.skala.shop.dto.cart;

import java.util.List;

public record CheckoutResponse(
        String message,
        long paidAmount,
        long remainingPoint,
        List<CartItemResponse> orderedItems
        ) {

}
