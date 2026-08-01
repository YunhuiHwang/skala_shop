package com.skala.shop.dto.cart;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record CheckoutRequest(
        @NotEmpty(message = "주문할 상품을 선택해 주세요.")
        List<Long> productIds
        ) {

}
