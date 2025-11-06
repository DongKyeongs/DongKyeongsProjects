package com.dkp.exchange.service;

import com.dkp.exchange.dto.AuthResponse;
import com.dkp.exchange.dto.LoginRequest;
import com.dkp.exchange.dto.RegisterRequest;
import com.dkp.exchange.model.User;
import com.dkp.exchange.model.UserRole;
import com.dkp.exchange.model.ExchangeSettings;
import com.dkp.exchange.repository.UserRepository;
import com.dkp.exchange.repository.ExchangeSettingsRepository;
import com.dkp.exchange.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final ExchangeSettingsRepository exchangeSettingsRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // 사용자명 중복 확인
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        // 사용자 생성
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(UserRole.USER);

        // 초기 잔액 설정 (테스트용)
        user.setUsdtBalance(new BigDecimal("10000")); // 10,000 USDT
        user.setBtcBalance(BigDecimal.ZERO);
        user.setEthBalance(BigDecimal.ZERO);

        User savedUser = userRepository.save(user);

        // 기본 설정 생성
        ExchangeSettings settings = new ExchangeSettings();
        settings.setUser(savedUser);
        exchangeSettingsRepository.save(settings);

        // JWT 토큰 생성
        String token = jwtUtil.generateToken(savedUser.getUsername());

        return AuthResponse.builder()
                .token(token)
                .username(savedUser.getUsername())
                .role(savedUser.getRole().name())
                .usdtBalance(savedUser.getUsdtBalance())
                .btcBalance(savedUser.getBtcBalance())
                .ethBalance(savedUser.getEthBalance())
                .build();
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        // 인증 수행
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 사용자 조회
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // JWT 토큰 생성
        String token = jwtUtil.generateToken(user.getUsername());

        return AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .role(user.getRole().name())
                .usdtBalance(user.getUsdtBalance())
                .btcBalance(user.getBtcBalance())
                .ethBalance(user.getEthBalance())
                .build();
    }

    public void logout() {
        SecurityContextHolder.clearContext();
    }

    @Transactional(readOnly = true)
    public AuthResponse getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Not authenticated");
        }

        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return AuthResponse.builder()
                .username(user.getUsername())
                .role(user.getRole().name())
                .usdtBalance(user.getUsdtBalance())
                .btcBalance(user.getBtcBalance())
                .ethBalance(user.getEthBalance())
                .build();
    }
}
