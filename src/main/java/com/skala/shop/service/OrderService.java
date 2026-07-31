package com.skala.shop.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skala.shop.domain.customer.Customer;
import com.skala.shop.domain.customer.CustomerRepository;
import com.skala.shop.domain.order.OrderItem;
import com.skala.shop.domain.order.OrderItemRepository;
import com.skala.shop.domain.product.Product;
import com.skala.shop.domain.product.ProductRepository;
import com.skala.shop.dto.order.CustomerOrderResponse;
import com.skala.shop.dto.order.OrderItemResponse;
import com.skala.shop.dto.order.OrderRequest;
import com.skala.shop.dto.order.OrderResultResponse;
import com.skala.shop.exception.BusinessException;
import com.skala.shop.exception.ErrorCode;

@Service
@Transactional(readOnly = true)
public class OrderService {

    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;

    public OrderService(
            CustomerRepository customerRepository,
            ProductRepository productRepository,
            OrderItemRepository orderItemRepository) {
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.orderItemRepository = orderItemRepository;
    }

    // 내 주문 목록 조회
    public CustomerOrderResponse findMyOrders(String customerId) {
        Customer customer = getCustomer(customerId);
        List<OrderItemResponse> orders = orderItemRepository.findAllByCustomer(customer).stream()
                .map(OrderItemResponse::from)
                .toList();
        return new CustomerOrderResponse(customer.getCustomerId(), customer.getPoint(), orders);
    }

    // 상품 주문 (포인트 차감 + 수량 누적)
    @Transactional
    public OrderResultResponse placeOrder(String customerId, OrderRequest request) {
        Customer customer = getCustomer(customerId);
        Product product = getProduct(request.productId());

        // 주문 총액 계산
        long orderPrice = Math.multiplyExact(product.getPrice(), (long) request.quantity());
        // 포인트 부족 시 주문 거부
        if (customer.getPoint() < orderPrice) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_FUNDS);
        }

        // 포인트 차감
        customer.usePoint(orderPrice);
        // 기존 주문 있으면 수량 누적, 없으면 새로 생성
        OrderItem orderItem = orderItemRepository.findByCustomerAndProduct(customer, product)
                .orElseGet(() -> new OrderItem(customer, product, 0));
        orderItem.addQuantity(request.quantity());
        orderItemRepository.save(orderItem);

        return new OrderResultResponse(
                "상품 주문이 완료되었습니다.",
                customer.getPoint(),
                product.getId(),
                product.getName(),
                orderItem.getQuantity());
    }

    // 주문 취소 (수량 감소 + 포인트 환급)
    @Transactional
    public OrderResultResponse cancelOrder(String customerId, OrderRequest request) {
        Customer customer = getCustomer(customerId);
        Product product = getProduct(request.productId());

        // 주문 내역 없으면 취소 불가
        OrderItem orderItem = orderItemRepository.findByCustomerAndProduct(customer, product)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
        // 주문 수량보다 많이 취소하면 거부
        if (orderItem.getQuantity() < request.quantity()) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_QUANTITY);
        }

        // 환급액 계산 후 수량 감소 + 포인트 환급
        long refundPrice = Math.multiplyExact(product.getPrice(), (long) request.quantity());
        orderItem.decreaseQuantity(request.quantity());
        customer.refundPoint(refundPrice);

        // 수량이 0이면 주문 행 삭제
        int remainingQuantity = orderItem.getQuantity();
        if (remainingQuantity == 0) {
            orderItemRepository.delete(orderItem);
        }

        return new OrderResultResponse(
                "주문 취소가 완료되었습니다.",
                customer.getPoint(),
                product.getId(),
                product.getName(),
                remainingQuantity);
    }

    // 고객 조회 (없으면 404)
    private Customer getCustomer(String customerId) {
        return customerRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CUSTOMER_NOT_FOUND));
    }

    // 상품 조회 (없으면 404)
    private Product getProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
    }
}
