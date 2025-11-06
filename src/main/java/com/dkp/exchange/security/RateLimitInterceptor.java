package com.dkp.exchange.security;

import com.dkp.exchange.exception.RateLimitExceededException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {
    private final RedisTemplate<String, String> redisTemplate;

    // 기본 Rate Limit: 60 requests per minute
    private static final int MAX_REQUESTS_PER_MINUTE = 60;
    private static final int MAX_REQUESTS_PER_SECOND = 10;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String clientId = getClientId(request);
        String endpoint = request.getRequestURI();

        // 분당 제한 체크
        if (!checkRateLimit(clientId, "minute", MAX_REQUESTS_PER_MINUTE, 60)) {
            throw new RateLimitExceededException(
                    String.format("Rate limit exceeded: %d requests per minute", MAX_REQUESTS_PER_MINUTE));
        }

        // 초당 제한 체크 (버스트 방지)
        if (!checkRateLimit(clientId, "second", MAX_REQUESTS_PER_SECOND, 1)) {
            throw new RateLimitExceededException(
                    String.format("Rate limit exceeded: %d requests per second", MAX_REQUESTS_PER_SECOND));
        }

        return true;
    }

    private boolean checkRateLimit(String clientId, String timeUnit, int maxRequests, int ttlSeconds) {
        String key = String.format("rate_limit:%s:%s:%s", clientId, timeUnit, System.currentTimeMillis() / (ttlSeconds * 1000));

        Long count = redisTemplate.opsForValue().increment(key);
        if (count == null) {
            count = 0L;
        }

        if (count == 1) {
            // 첫 요청이면 TTL 설정
            redisTemplate.expire(key, Duration.ofSeconds(ttlSeconds));
        }

        if (count > maxRequests) {
            log.warn("Rate limit exceeded for client: {} (count: {})", clientId, count);
            return false;
        }

        return true;
    }

    private String getClientId(HttpServletRequest request) {
        // API Key가 있으면 사용, 없으면 IP 주소 사용
        String apiKey = request.getHeader("X-API-Key");
        if (apiKey != null && !apiKey.isEmpty()) {
            return "api:" + apiKey;
        }

        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }

        return "ip:" + ip;
    }
}
