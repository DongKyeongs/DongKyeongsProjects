package com.dkp.exchange.service;

import com.dkp.exchange.exception.UnauthorizedAccessException;
import com.dkp.exchange.exception.UserNotFoundException;
import com.dkp.exchange.model.ApiKey;
import com.dkp.exchange.model.User;
import com.dkp.exchange.repository.ApiKeyRepository;
import com.dkp.exchange.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApiKeyService {
    private final ApiKeyRepository apiKeyRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public ApiKey createApiKey(Long userId, String name, boolean canTrade, boolean canWithdraw, String ipWhitelist) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        ApiKey apiKey = new ApiKey();
        apiKey.setUser(user);
        apiKey.setName(name);
        apiKey.setCanRead(true);
        apiKey.setCanTrade(canTrade);
        apiKey.setCanWithdraw(canWithdraw);
        apiKey.setIpWhitelist(ipWhitelist);
        apiKey.setActive(true);

        // Secret key를 암호화하여 저장
        String secretKey = apiKey.getSecretKey();
        apiKey.setSecretKey(passwordEncoder.encode(secretKey));

        ApiKey saved = apiKeyRepository.save(apiKey);

        log.info("API Key created for user {}: {}", userId, name);

        // 실제 secret key는 한번만 반환 (DB에는 암호화된 값 저장)
        saved.setSecretKey(secretKey);
        return saved;
    }

    @Transactional(readOnly = true)
    public ApiKey validateApiKey(String apiKeyStr, String clientIp) {
        ApiKey apiKey = apiKeyRepository.findByApiKey(apiKeyStr)
                .orElseThrow(() -> new UnauthorizedAccessException("Invalid API key"));

        if (!apiKey.isActive()) {
            throw new UnauthorizedAccessException("API key is disabled");
        }

        if (apiKey.isExpired()) {
            throw new UnauthorizedAccessException("API key has expired");
        }

        if (!apiKey.isIpAllowed(clientIp)) {
            log.warn("IP {} not allowed for API key {}", clientIp, apiKeyStr);
            throw new UnauthorizedAccessException("IP address not whitelisted");
        }

        return apiKey;
    }

    @Transactional
    public void updateLastUsed(String apiKeyStr) {
        apiKeyRepository.findByApiKey(apiKeyStr).ifPresent(apiKey -> {
            apiKey.setLastUsedAt(LocalDateTime.now());
            apiKeyRepository.save(apiKey);
        });
    }

    @Transactional
    public void revokeApiKey(Long userId, Long apiKeyId) {
        ApiKey apiKey = apiKeyRepository.findById(apiKeyId)
                .orElseThrow(() -> new RuntimeException("API key not found"));

        if (!apiKey.getUser().getId().equals(userId)) {
            throw new UnauthorizedAccessException("Cannot revoke API key of another user");
        }

        apiKey.setActive(false);
        apiKeyRepository.save(apiKey);

        log.info("API Key revoked: {} for user {}", apiKeyId, userId);
    }

    @Transactional(readOnly = true)
    public List<ApiKey> getUserApiKeys(Long userId) {
        return apiKeyRepository.findByUserId(userId);
    }
}
