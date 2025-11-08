package com.dkp.exchange.service;

import com.dkp.exchange.model.*;
import com.dkp.exchange.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 수수료 분석 서비스
 * - 사용자별 수수료 납부 내역
 * - 심볼별 수수료 수익 통계
 * - VIP 등급별 수수료 현황
 * - 수수료 트렌드 분석
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FeeAnalyticsService {
    private final FeeTransactionRepository feeTransactionRepository;
    private final TradeRepository tradeRepository;
    private final UserRepository userRepository;
    private final FeeRefundRepository feeRefundRepository;

    /**
     * 사용자별 수수료 납부 내역
     */
    @Transactional(readOnly = true)
    public UserFeeStats getUserFeeStats(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<FeeTransaction> feeTransactions = feeTransactionRepository.findByUserId(userId);

        // 총 수수료 납부액
        BigDecimal totalFees = feeTransactions.stream()
                .map(FeeTransaction::getFeeAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 타입별 수수료 합산
        Map<Fee.FeeType, BigDecimal> feesByType = feeTransactions.stream()
                .collect(Collectors.groupingBy(
                        FeeTransaction::getFeeType,
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                FeeTransaction::getFeeAmount,
                                BigDecimal::add
                        )
                ));

        // 수수료 환급액
        BigDecimal totalRefunded = feeRefundRepository.getTotalRefundedByUserId(userId);
        if (totalRefunded == null) totalRefunded = BigDecimal.ZERO;

        // 순 수수료
        BigDecimal netFees = totalFees.subtract(totalRefunded);

        return UserFeeStats.builder()
                .userId(userId)
                .username(user.getUsername())
                .vipLevel(user.getVipLevel())
                .totalFeesPaid(totalFees)
                .makerFees(feesByType.getOrDefault(Fee.FeeType.MAKER, BigDecimal.ZERO))
                .takerFees(feesByType.getOrDefault(Fee.FeeType.TAKER, BigDecimal.ZERO))
                .withdrawalFees(feesByType.getOrDefault(Fee.FeeType.WITHDRAWAL, BigDecimal.ZERO))
                .totalRefunded(totalRefunded)
                .netFees(netFees)
                .transactionCount(feeTransactions.size())
                .build();
    }

    /**
     * 심볼별 수수료 수익 통계
     */
    @Transactional(readOnly = true)
    public List<SymbolFeeStats> getSymbolFeeStats() {
        List<FeeTransaction> allFeeTransactions = feeTransactionRepository.findAll();

        Map<String, List<FeeTransaction>> bySymbol = allFeeTransactions.stream()
                .collect(Collectors.groupingBy(FeeTransaction::getSymbol));

        return bySymbol.entrySet().stream()
                .map(entry -> {
                    String symbol = entry.getKey();
                    List<FeeTransaction> transactions = entry.getValue();

                    BigDecimal totalFees = transactions.stream()
                            .map(FeeTransaction::getFeeAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    return SymbolFeeStats.builder()
                            .symbol(symbol)
                            .totalFees(totalFees)
                            .transactionCount(transactions.size())
                            .build();
                })
                .sorted((a, b) -> b.getTotalFees().compareTo(a.getTotalFees()))
                .collect(Collectors.toList());
    }

    /**
     * VIP 등급별 수수료 현황
     */
    @Transactional(readOnly = true)
    public List<VipFeeStats> getVipFeeStats() {
        List<User> allUsers = userRepository.findAll();

        Map<VipLevel, List<User>> byVipLevel = allUsers.stream()
                .collect(Collectors.groupingBy(User::getVipLevel));

        return byVipLevel.entrySet().stream()
                .map(entry -> {
                    VipLevel vipLevel = entry.getKey();
                    List<User> users = entry.getValue();

                    BigDecimal totalFees = users.stream()
                            .map(User::getTotalFeesPaid)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    BigDecimal totalRefunded = users.stream()
                            .map(User::getTotalFeesRefunded)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    return VipFeeStats.builder()
                            .vipLevel(vipLevel)
                            .userCount(users.size())
                            .totalFeesPaid(totalFees)
                            .totalRefunded(totalRefunded)
                            .netRevenue(totalFees.subtract(totalRefunded))
                            .build();
                })
                .sorted(Comparator.comparing(VipFeeStats::getVipLevel))
                .collect(Collectors.toList());
    }

    /**
     * 일별 수수료 수익 (최근 30일)
     */
    @Transactional(readOnly = true)
    public List<DailyFeeStats> getDailyFeeStats(int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        List<FeeTransaction> recentTransactions = feeTransactionRepository.findAll().stream()
                .filter(ft -> ft.getCreatedAt().isAfter(startDate))
                .toList();

        Map<String, List<FeeTransaction>> byDate = recentTransactions.stream()
                .collect(Collectors.groupingBy(
                        ft -> ft.getCreatedAt().toLocalDate().toString()
                ));

        return byDate.entrySet().stream()
                .map(entry -> {
                    String date = entry.getKey();
                    List<FeeTransaction> transactions = entry.getValue();

                    BigDecimal totalFees = transactions.stream()
                            .map(FeeTransaction::getFeeAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    return DailyFeeStats.builder()
                            .date(date)
                            .totalFees(totalFees)
                            .transactionCount(transactions.size())
                            .build();
                })
                .sorted(Comparator.comparing(DailyFeeStats::getDate))
                .collect(Collectors.toList());
    }

    /**
     * 전체 수수료 요약
     */
    @Transactional(readOnly = true)
    public OverallFeeStats getOverallFeeStats() {
        List<FeeTransaction> allTransactions = feeTransactionRepository.findAll();

        BigDecimal totalFees = allTransactions.stream()
                .map(FeeTransaction::getFeeAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<Fee.FeeType, BigDecimal> byType = allTransactions.stream()
                .collect(Collectors.groupingBy(
                        FeeTransaction::getFeeType,
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                FeeTransaction::getFeeAmount,
                                BigDecimal::add
                        )
                ));

        List<FeeRefund> allRefunds = feeRefundRepository.findAll();
        BigDecimal totalRefunded = allRefunds.stream()
                .filter(r -> r.getStatus() == FeeRefund.RefundStatus.PROCESSED)
                .map(FeeRefund::getRefundAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return OverallFeeStats.builder()
                .totalFees(totalFees)
                .makerFees(byType.getOrDefault(Fee.FeeType.MAKER, BigDecimal.ZERO))
                .takerFees(byType.getOrDefault(Fee.FeeType.TAKER, BigDecimal.ZERO))
                .withdrawalFees(byType.getOrDefault(Fee.FeeType.WITHDRAWAL, BigDecimal.ZERO))
                .totalRefunded(totalRefunded)
                .netRevenue(totalFees.subtract(totalRefunded))
                .transactionCount(allTransactions.size())
                .build();
    }

    // DTO Classes
    @lombok.Builder
    @lombok.Data
    public static class UserFeeStats {
        private Long userId;
        private String username;
        private VipLevel vipLevel;
        private BigDecimal totalFeesPaid;
        private BigDecimal makerFees;
        private BigDecimal takerFees;
        private BigDecimal withdrawalFees;
        private BigDecimal totalRefunded;
        private BigDecimal netFees;
        private int transactionCount;
    }

    @lombok.Builder
    @lombok.Data
    public static class SymbolFeeStats {
        private String symbol;
        private BigDecimal totalFees;
        private int transactionCount;
    }

    @lombok.Builder
    @lombok.Data
    public static class VipFeeStats {
        private VipLevel vipLevel;
        private int userCount;
        private BigDecimal totalFeesPaid;
        private BigDecimal totalRefunded;
        private BigDecimal netRevenue;
    }

    @lombok.Builder
    @lombok.Data
    public static class DailyFeeStats {
        private String date;
        private BigDecimal totalFees;
        private int transactionCount;
    }

    @lombok.Builder
    @lombok.Data
    public static class OverallFeeStats {
        private BigDecimal totalFees;
        private BigDecimal makerFees;
        private BigDecimal takerFees;
        private BigDecimal withdrawalFees;
        private BigDecimal totalRefunded;
        private BigDecimal netRevenue;
        private int transactionCount;
    }
}
