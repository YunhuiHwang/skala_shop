package com.skala.shop.dto.cart;

import java.util.List;

public record CartResponse(
        String customerId,
        List<CartItemResponse> items,
        long totalPrice
        ) {

}
