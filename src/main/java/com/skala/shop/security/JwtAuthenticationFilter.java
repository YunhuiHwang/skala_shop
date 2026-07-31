package com.skala.shop.security;

import java.util.List;
import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.JwtException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component // 요청마다 JWT를 검사하는 필터
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // Authorization 헤더에서 토큰 추출
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith(BEARER_PREFIX)) {
            String token = authorization.substring(BEARER_PREFIX.length());
            try {

                // 토큰 검증 후 고객 ID 추출
                String customerId = jwtTokenProvider.getCustomerId(token);
                var authentication = new UsernamePasswordAuthenticationToken(
                        customerId,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER")));

                // 인증 정보 저장 (이후 Controller에서 사용)
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (JwtException | IllegalArgumentException exception) {
                
                // 토큰이 유효하지 않으면 인증 정보 제거
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response); // 다음 필터로 전달
    }
}
