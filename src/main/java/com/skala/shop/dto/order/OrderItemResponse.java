package com.skala.shop.dto.order;

import com.skala.shop.domain.order.OrderItem;

public record OrderItemResponse(
        Long productId,
        String productName,
        long unitPrice,
        int quantity,
        long totalPrice) {

    public static OrderItemResponse from(OrderItem orderItem) {
        long totalPrice = Math.multiplyExact(
                orderItem.getProduct().getPrice(),
                (long) orderItem.getQuantity());

        return new OrderItemResponse(
                orderItem.getProduct().getId(),
                orderItem.getProduct().getName(),
                orderItem.getProduct().getPrice(),
                orderItem.getQuantity(),
                totalPrice);
    }
}
