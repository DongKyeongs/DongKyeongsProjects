package com.dkp.exchange.service;

import com.dkp.exchange.model.Trade;
import com.dkp.exchange.model.User;
import com.dkp.exchange.model.VipLevel;
import com.dkp.exchange.repository.TradeRepository;
import com.dkp.exchange.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * VIP 등급 관리 서비스
 * - 30일 거래량 계산
 * - 자동 VIP 등급 업그레이드/다운그레이드
 * - VIP 혜택 관리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VipService {
    private final UserRepository userRepository;
    private final TradeRepository tradeRepository;

    /**
     * 사용자의 최근 30일 거래량 계산
     */
    @Transactional(readOnly = true)
    public BigDecimal calculate30DayVolume(Long userId) {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);

        List<Trade> recentTrades = tradeRepository.findAll().stream()
                .filter(trade -> {
                    Long buyUserId = trade.getBuyOrder().getUser().getId();
                    Long sellUserId = trade.getSellOrder().getUser().getId();
                    boolean isUserTrade = buyUserId.equals(userId) || sellUserId.equals(userId);
                    boolean isRecent = trade.getExecutedAt().isAfter(thirtyDaysAgo);
                    return isUserTrade && isRecent;
                })
                .toList();

        // 거래량 합산 (USDT 기준)
        BigDecimal totalVolume = recentTrades.stream()
                .map(trade -> trade.getPrice().multiply(trade.getAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        log.debug("User {} 30-day volume: {}", userId, totalVolume);
        return totalVolume;
    }

    /**
     * 사용자의 VIP 등급 업데이트
     */
    @Transactional
    public VipLevel updateUserVipLevel(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 30일 거래량 계산
        BigDecimal volume30d = calculate30DayVolume(userId);

        // 기존 등급 저장
        VipLevel oldLevel = user.getVipLevel();

        // 새 등급 계산
        VipLevel newLevel = VipLevel.calculateVipLevel(volume30d);

        // 등급 변경 시 업데이트
        if (oldLevel != newLevel) {
            user.setVipLevel(newLevel);
            user.setTotalTradingVolume30d(volume30d);
            user.setLastVipUpdate(LocalDateTime.now());
            userRepository.save(user);

            log.info("User {} VIP level updated: {} -> {} (volume: {})",
                    userId, oldLevel, newLevel, volume30d);
        } else {
            // 등급은 같지만 거래량은 업데이트
            user.setTotalTradingVolume30d(volume30d);
            user.setLastVipUpdate(LocalDateTime.now());
            userRepository.save(user);
        }

        return newLevel;
    }

    /**
     * 모든 사용자의 VIP 등급 업데이트 (스케줄러)
     * 매일 자정에 실행
     */
    @Scheduled(cron = "0 0 0 * * *") // 매일 자정
    @Transactional
    public void updateAllUsersVipLevels() {
        log.info("Starting daily VIP level update for all users");

        List<User> allUsers = userRepository.findAll();
        int updatedCount = 0;

        for (User user : allUsers) {
            try {
                VipLevel oldLevel = user.getVipLevel();
                VipLevel newLevel = updateUserVipLevel(user.getId());

                if (oldLevel != newLevel) {
                    updatedCount++;
                }
            } catch (Exception e) {
                log.error("Failed to update VIP level for user {}: {}", user.getId(), e.getMessage());
            }
        }

        log.info("Completed daily VIP level update: {} users updated out of {}",
                updatedCount, allUsers.size());
    }

    /**
     * VIP 등급 정보 조회
     */
    @Transactional(readOnly = true)
    public VipInfo getVipInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        VipLevel currentLevel = user.getVipLevel();
        BigDecimal current30dVolume = calculate30DayVolume(userId);

        // 다음 등급 계산
        VipLevel nextLevel = getNextLevel(currentLevel);
        BigDecimal volumeToNextLevel = BigDecimal.ZERO;

        if (nextLevel != null) {
            BigDecimal requiredVolume = new BigDecimal(nextLevel.getRequiredVolume());
            volumeToNextLevel = requiredVolume.subtract(current30dVolume).max(BigDecimal.ZERO);
        }

        return VipInfo.builder()
                .currentLevel(currentLevel)
                .nextLevel(nextLevel)
                .current30dVolume(current30dVolume)
                .volumeToNextLevel(volumeToNextLevel)
                .discountPercent(currentLevel.getDiscountPercent())
                .lastUpdate(user.getLastVipUpdate())
                .build();
    }

    /**
     * 다음 VIP 등급 반환
     */
    private VipLevel getNextLevel(VipLevel currentLevel) {
        return switch (currentLevel) {
            case NONE -> VipLevel.VIP1;
            case VIP1 -> VipLevel.VIP2;
            case VIP2 -> VipLevel.VIP3;
            case VIP3 -> VipLevel.VIP4;
            case VIP4 -> VipLevel.VIP5;
            case VIP5 -> null; // 최고 등급
        };
    }

    /**
     * VIP 정보 DTO
     */
    @lombok.Builder
    @lombok.Data
    public static class VipInfo {
        private VipLevel currentLevel;
        private VipLevel nextLevel;
        private BigDecimal current30dVolume;
        private BigDecimal volumeToNextLevel;
        private String discountPercent;
        private LocalDateTime lastUpdate;
    }

    /**
     * 관리자: 수동으로 VIP 등급 설정
     */
    @Transactional
    public void setUserVipLevel(Long userId, VipLevel level) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        VipLevel oldLevel = user.getVipLevel();
        user.setVipLevel(level);
        user.setLastVipUpdate(LocalDateTime.now());
        userRepository.save(user);

        log.info("Admin set user {} VIP level: {} -> {}", userId, oldLevel, level);
    }
}
