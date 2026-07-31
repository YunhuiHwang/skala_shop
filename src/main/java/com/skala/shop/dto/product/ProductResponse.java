package com.skala.shop.dto.product;

import com.skala.shop.domain.product.Product;

public record ProductResponse(Long id, String name, long price) {

    public static ProductResponse from(Product product) {
        return new ProductResponse(product.getId(), product.getName(), product.getPrice());
    }
}
