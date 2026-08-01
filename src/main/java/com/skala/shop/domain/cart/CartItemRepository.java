package com.skala.shop.domain.cart;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.skala.shop.domain.customer.Customer;
import com.skala.shop.domain.product.Product;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    // 특정 고객의 특정 상품 카트 항목 (담기/수정/삭제 시)
    Optional<CartItem> findByCustomerAndProduct(Customer customer, Product product);

    // 특정 고객의 카트 전체 (조회/checkout 시)
    List<CartItem> findAllByCustomer(Customer customer);
}
