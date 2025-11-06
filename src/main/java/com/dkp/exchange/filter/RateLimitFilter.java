package com.dkp.exchange.filter;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final Map<String, Bucket> rateLimitBuckets;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String clientId = getClientId(request);
        Bucket bucket = rateLimitBuckets.computeIfAbsent(clientId, k -> createNewBucket());

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            response.addHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(HttpServletResponse.SC_TOO_MANY_REQUESTS);
            response.addHeader("X-Rate-Limit-Retry-After-Seconds",
                String.valueOf(probe.getNanosToWaitForRefill() / 1_000_000_000));
            response.getWriter().write("Too many requests - Rate limit exceeded");
        }
    }

    private String getClientId(HttpServletRequest request) {
        String clientId = request.getHeader("X-Forwarded-For");
        if (clientId == null || clientId.isEmpty()) {
            clientId = request.getRemoteAddr();
        }
        return clientId;
    }

    private Bucket createNewBucket() {
        return io.github.bucket4j.Bucket.builder()
            .addLimit(io.github.bucket4j.Bandwidth.classic(60,
                io.github.bucket4j.Refill.intervally(60, java.time.Duration.ofMinutes(1))))
            .build();
    }
}
