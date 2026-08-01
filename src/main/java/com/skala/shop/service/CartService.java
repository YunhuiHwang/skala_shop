package com.skala.shop.service;

import com.skala.shop.domain.cart.CartItem;
import com.skala.shop.domain.cart.CartItemRepository;
import com.skala.shop.domain.customer.Customer;
import com.skala.shop.domain.customer.CustomerRepository;
import com.skala.shop.domain.product.Product;
import com.skala.shop.domain.product.ProductRepository;
import com.skala.shop.domain.order.OrderItem;
import com.skala.shop.domain.order.OrderItemRepository;
import com.skala.shop.dto.cart.CheckoutRequest;
import com.skala.shop.dto.cart.CheckoutResponse;
import com.skala.shop.dto.cart.CartItemRequest;
import com.skala.shop.dto.cart.CartItemResponse;
import com.skala.shop.dto.cart.CartResponse;
import com.skala.shop.exception.BusinessException;
import com.skala.shop.exception.ErrorCode;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CartService {

    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderItemRepository orderItemRepository;

    public CartService(
            CustomerRepository customerRepository,
            ProductRepository productRepository,
            CartItemRepository cartItemRepository,
            OrderItemRepository orderItemRepository
    ) {
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.cartItemRepository = cartItemRepository;
        this.orderItemRepository = orderItemRepository;
    }

    // 카트 조회 (담긴 항목 + 총액)
    public CartResponse getCart(String customerId) {
        Customer customer = getCustomer(customerId);
        List<CartItemResponse> items = cartItemRepository.findAllByCustomer(customer).stream()
                .map(CartItemResponse::from)
                .toList();
        long totalPrice = items.stream()
                .mapToLong(CartItemResponse::subtotalPrice)
                .sum();
        return new CartResponse(customer.getCustomerId(), items, totalPrice);
    }

    // 카트에 추가 (이미 있으면 수량 누적)
    @Transactional
    public CartResponse addToCart(String customerId, CartItemRequest request) {
        Customer customer = getCustomer(customerId);
        Product product = getProduct(request.productId());

        CartItem cartItem = cartItemRepository.findByCustomerAndProduct(customer, product)
                .orElseGet(() -> new CartItem(customer, product, 0));
        cartItem.addQuantity(request.quantity());
        cartItemRepository.save(cartItem);

        return getCart(customerId);
    }

    // 수량 변경 (특정 값으로 덮어쓰기)
    @Transactional
    public CartResponse updateQuantity(String customerId, Long productId, int quantity) {
        Customer customer = getCustomer(customerId);
        Product product = getProduct(productId);

        CartItem cartItem = cartItemRepository.findByCustomerAndProduct(customer, product)
                .orElseThrow(() -> new BusinessException(ErrorCode.CART_ITEM_NOT_FOUND));
        cartItem.changeQuantity(quantity);

        return getCart(customerId);
    }

    // 카트에서 특정 상품 삭제
    @Transactional
    public CartResponse removeFromCart(String customerId, Long productId) {
        Customer customer = getCustomer(customerId);
        Product product = getProduct(productId);

        CartItem cartItem = cartItemRepository.findByCustomerAndProduct(customer, product)
                .orElseThrow(() -> new BusinessException(ErrorCode.CART_ITEM_NOT_FOUND));
        cartItemRepository.delete(cartItem);

        return getCart(customerId);
    }

    // 카트 비우기 (전체 삭제)
    @Transactional
    public void clearCart(String customerId) {
        Customer customer = getCustomer(customerId);
        List<CartItem> items = cartItemRepository.findAllByCustomer(customer);
        cartItemRepository.deleteAll(items);
    }

    // 카트에서 선택 상품 한 번에 주문
    @Transactional
    public CheckoutResponse checkout(String customerId, CheckoutRequest request) {
        Customer customer = getCustomer(customerId);

        // 선택한 상품의 카트 항목 선택
        List<CartItem> targets = request.productIds().stream()
                .map(productId -> {
                    Product product = getProduct(productId);
                    return cartItemRepository.findByCustomerAndProduct(customer, product)
                            .orElseThrow(() -> new BusinessException(ErrorCode.CART_ITEM_NOT_FOUND));
                })
                .toList();

        if (targets.isEmpty()) {
            throw new BusinessException(ErrorCode.EMPTY_CART);
        }

        // 총 결제액 계산
        long totalPrice = targets.stream()
                .mapToLong(item -> Math.multiplyExact(
                item.getProduct().getPrice(), (long) item.getQuantity()))
                .sum();

        // 포인트 부족 시 전체 거부
        if (customer.getPoint() < totalPrice) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_FUNDS);
        }

        // 포인트 차감
        customer.usePoint(totalPrice);

        // 주문으로 이동 후 카트에서 제거
        List<CartItemResponse> orderedItems = targets.stream()
                .map(cartItem -> {
                    Product product = cartItem.getProduct();
                    OrderItem orderItem = orderItemRepository
                            .findByCustomerAndProduct(customer, product)
                            .orElseGet(() -> new OrderItem(customer, product, 0));
                    orderItem.addQuantity(cartItem.getQuantity());
                    orderItemRepository.save(orderItem);

                    CartItemResponse response = CartItemResponse.from(cartItem);
                    cartItemRepository.delete(cartItem);
                    return response;
                })
                .toList();

        return new CheckoutResponse(
                "선택한 상품 주문이 완료되었습니다.",
                totalPrice,
                customer.getPoint(),
                orderedItems
        );
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
