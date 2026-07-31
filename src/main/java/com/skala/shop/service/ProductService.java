package com.skala.shop.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skala.shop.domain.product.Product;
import com.skala.shop.domain.product.ProductRepository;
import com.skala.shop.dto.product.ProductRequest;
import com.skala.shop.dto.product.ProductResponse;
import com.skala.shop.exception.BusinessException;
import com.skala.shop.exception.ErrorCode;

@Service
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;

    // 생성자 주입(저장소 직접 구현 X)
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // 상품 전체 조회
    public List<ProductResponse> findAll() {
        return productRepository.findAll().stream().map(ProductResponse::from).toList();
    }

    // 상품 단건 조회(id)
    public ProductResponse findById(Long id) {
        return ProductResponse.from(getProduct(id));
    }

    // 상품 등록
    @Transactional
    public ProductResponse create(ProductRequest request) {
        Product product = new Product(request.name(), request.price());
        return ProductResponse.from(productRepository.save(product));
    }

    // 상품 수정
    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = getProduct(id);
        product.update(request.name(), request.price());
        return ProductResponse.from(product);
    }

    // 상품 삭제
    @Transactional
    public void delete(Long id){
        Product product = getProduct(id);
        productRepository.delete(product);
    }

    // 상품 조회 공통 메서드 (없으면 예외 발생)
    private Product getProduct(Long id) {
        return productRepository.findById(id).orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
    }
}
