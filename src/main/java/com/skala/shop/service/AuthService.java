package com.skala.shop.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skala.shop.domain.customer.Customer;
import com.skala.shop.domain.customer.CustomerRepository;
import com.skala.shop.dto.customer.LoginRequest;
import com.skala.shop.dto.customer.LoginResponse;
import com.skala.shop.dto.customer.SignUpRequest;
import com.skala.shop.dto.customer.SignUpResponse;
import com.skala.shop.exception.BusinessException;
import com.skala.shop.exception.ErrorCode;
import com.skala.shop.security.JwtTokenProvider;

@Service
@Transactional(readOnly = true)
public class AuthService {

    private static final long INITIAL_POINT = 1_000_000L;

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final long expirationMinutes;

    public AuthService(
            CustomerRepository customerRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider,
            @Value("${jwt.expiration-minutes}") long expirationMinutes) {
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.expirationMinutes = expirationMinutes;
    }

    // 회원가입
    @Transactional
    public SignUpResponse signUp(SignUpRequest request) {

        // 중복 ID 검사
        if (customerRepository.existsByCustomerId(request.customerId())) {
            throw new BusinessException(ErrorCode.DUPLICATE_CUSTOMER_ID);
        }

        // 비밀번호 BCrypt 해시로 저장 + 초기 포인트 지급
        Customer customer = new Customer(
                request.customerId(),
                passwordEncoder.encode(request.password()),
                INITIAL_POINT);
        customerRepository.save(customer);
        return new SignUpResponse(
                customer.getCustomerId(),
                customer.getPoint(),
                "회원가입이 완료되었습니다.");
    }

    // 로그인
    public LoginResponse login(LoginRequest request) {

        // ID로 고객 조회 (없으면 인증 실패)
        Customer customer = customerRepository.findByCustomerId(request.customerId())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        // 입력 비밀번호와 저장된 해시 비교
        if (!passwordEncoder.matches(request.password(), customer.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        // 검증 성공 → JWT 발급
        String token = jwtTokenProvider.generateToken(customer.getCustomerId());
        return new LoginResponse(token, "Bearer", expirationMinutes);
    }
}
