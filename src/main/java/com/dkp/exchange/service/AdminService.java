package com.dkp.exchange.service;

import com.dkp.exchange.model.*;
import com.dkp.exchange.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {
    private final UserRepository userRepository;
    private final FeeRepository feeRepository;
    private final KycVerificationRepository kycRepository;
    private final AmlAlertRepository amlAlertRepository;
    private final TradeRepository tradeRepository;
    private final TransactionRepository transactionRepository;
    private final OrderRepository orderRepository;

    // ==================== 유저 관리 ====================

    @Transactional(readOnly = true)
    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public List<User> searchUsers(String keyword) {
        return userRepository.findByUsernameContaining(keyword);
    }

    @Transactional(readOnly = true)
    public User getUserDetails(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Transactional
    public void toggleUserEnabled(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setEnabled(!user.isEnabled());
        userRepository.save(user);
        log.info("User {} enabled status changed to: {}", userId, user.isEnabled());
    }

    @Transactional
    public void setTradingEnabled(Long userId, boolean enabled) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setTradingEnabled(enabled);
        userRepository.save(user);
        log.info("User {} trading enabled: {}", userId, enabled);
    }

    @Transactional
    public void setWithdrawalEnabled(Long userId, boolean enabled) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setWithdrawalEnabled(enabled);
        userRepository.save(user);
        log.info("User {} withdrawal enabled: {}", userId, enabled);
    }

    // ==================== 수수료 관리 ====================

    @Transactional(readOnly = true)
    public List<Fee> getAllFees() {
        return feeRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Fee> getFeesBySymbol(String symbol) {
        return feeRepository.findBySymbol(symbol);
    }

    @Transactional
    public Fee createFee(Fee fee) {
        Fee saved = feeRepository.save(fee);
        log.info("Fee created: {} for {}", fee.getType(), fee.getSymbol());
        return saved;
    }

    @Transactional
    public Fee updateFee(Long feeId, Fee feeData) {
        Fee fee = feeRepository.findById(feeId)
                .orElseThrow(() -> new RuntimeException("Fee not found"));

        fee.setSymbol(feeData.getSymbol());
        fee.setType(feeData.getType());
        fee.setRate(feeData.getRate());
        fee.setMinFee(feeData.getMinFee());
        fee.setMaxFee(feeData.getMaxFee());
        fee.setActive(feeData.isActive());

        Fee updated = feeRepository.save(fee);
        log.info("Fee updated: {}", feeId);
        return updated;
    }

    @Transactional
    public void deleteFee(Long feeId) {
        feeRepository.deleteById(feeId);
        log.info("Fee deleted: {}", feeId);
    }

    @Transactional
    public void toggleFeeActive(Long feeId) {
        Fee fee = feeRepository.findById(feeId)
                .orElseThrow(() -> new RuntimeException("Fee not found"));
        fee.setActive(!fee.isActive());
        feeRepository.save(fee);
        log.info("Fee {} active status: {}", feeId, fee.isActive());
    }

    // ==================== KYC/AML 관리 ====================

    @Transactional(readOnly = true)
    public List<KycVerification> getPendingKycVerifications() {
        return kycRepository.findPendingVerifications();
    }

    @Transactional(readOnly = true)
    public List<KycVerification> getAllKycVerifications() {
        return kycRepository.findAll();
    }

    @Transactional(readOnly = true)
    public KycVerification getKycVerification(Long kycId) {
        return kycRepository.findById(kycId)
                .orElseThrow(() -> new RuntimeException("KYC not found"));
    }

    @Transactional(readOnly = true)
    public List<AmlAlert> getOpenAmlAlerts() {
        return amlAlertRepository.findOpenAlerts();
    }

    @Transactional(readOnly = true)
    public List<AmlAlert> getAllAmlAlerts() {
        return amlAlertRepository.findAll();
    }

    @Transactional
    public void updateAmlAlertStatus(Long alertId, AmlAlert.AlertStatus status, String notes) {
        AmlAlert alert = amlAlertRepository.findById(alertId)
                .orElseThrow(() -> new RuntimeException("AML Alert not found"));

        alert.setStatus(status);
        if (notes != null) {
            alert.setInvestigationNotes(notes);
        }

        if (status == AmlAlert.AlertStatus.RESOLVED ||
            status == AmlAlert.AlertStatus.FALSE_POSITIVE) {
            alert.setResolvedAt(LocalDateTime.now());
        }

        amlAlertRepository.save(alert);
        log.info("AML Alert {} status updated to: {}", alertId, status);
    }

    // ==================== 통계 및 대시보드 ====================

    @Transactional(readOnly = true)
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        // 전체 유저 수
        long totalUsers = userRepository.count();
        stats.put("totalUsers", totalUsers);

        // 오늘 신규 가입자
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        // Note: UserRepository에 메서드 추가 필요
        stats.put("newUsersToday", 0L); // placeholder

        // 활성 유저
        long activeUsers = userRepository.countByEnabled(true);
        stats.put("activeUsers", activeUsers);

        // Pending KYC
        long pendingKyc = kycRepository.countPendingVerifications();
        stats.put("pendingKyc", pendingKyc);

        // Open AML Alerts
        long openAmlAlerts = amlAlertRepository.countOpenAlerts();
        stats.put("openAmlAlerts", openAmlAlerts);

        // 전체 거래 수
        long totalTrades = tradeRepository.count();
        stats.put("totalTrades", totalTrades);

        // 오늘 거래량
        List<Trade> todayTrades = tradeRepository.findAll().stream()
                .filter(t -> t.getExecutedAt().isAfter(todayStart))
                .toList();

        BigDecimal todayVolume = todayTrades.stream()
                .map(Trade::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.put("todayVolume", todayVolume);

        // 오늘 수수료 수익
        BigDecimal todayFees = todayTrades.stream()
                .map(Trade::getFee)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.put("todayFees", todayFees);

        // 전체 주문 수
        long totalOrders = orderRepository.count();
        stats.put("totalOrders", totalOrders);

        // 활성 주문 수 (PENDING)
        long activeOrders = orderRepository.countByStatus(Order.OrderStatus.PENDING);
        stats.put("activeOrders", activeOrders);

        log.info("Dashboard stats generated");
        return stats;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getRecentTrades(int limit) {
        List<Trade> trades = tradeRepository.findAll();

        return trades.stream()
                .sorted((t1, t2) -> t2.getExecutedAt().compareTo(t1.getExecutedAt()))
                .limit(limit)
                .map(trade -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", trade.getId());
                    map.put("symbol", trade.getSymbol());
                    map.put("price", trade.getPrice());
                    map.put("amount", trade.getAmount());
                    map.put("side", trade.getSide());
                    map.put("executedAt", trade.getExecutedAt());
                    return map;
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getRecentUsers(int limit) {
        List<User> users = userRepository.findAll();

        return users.stream()
                .sorted((u1, u2) -> u2.getId().compareTo(u1.getId()))
                .limit(limit)
                .map(user -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", user.getId());
                    map.put("username", user.getUsername());
                    map.put("role", user.getRole());
                    map.put("enabled", user.isEnabled());
                    return map;
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public Map<String, BigDecimal> getTradingVolumeBySymbol() {
        Map<String, BigDecimal> volumeMap = new HashMap<>();

        List<Trade> allTrades = tradeRepository.findAll();

        for (Trade trade : allTrades) {
            String symbol = trade.getSymbol();
            BigDecimal volume = trade.getAmount();
            volumeMap.merge(symbol, volume, BigDecimal::add);
        }

        return volumeMap;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getTopTraders(int limit) {
        Map<Long, BigDecimal> userVolumeMap = new HashMap<>();

        List<Trade> allTrades = tradeRepository.findAll();

        for (Trade trade : allTrades) {
            Long userId = trade.getBuyOrder().getUser().getId();
            BigDecimal volume = trade.getAmount().multiply(trade.getPrice());
            userVolumeMap.merge(userId, volume, BigDecimal::add);
        }

        return userVolumeMap.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(limit)
                .map(entry -> {
                    User user = userRepository.findById(entry.getKey()).orElse(null);
                    Map<String, Object> map = new HashMap<>();
                    map.put("userId", entry.getKey());
                    map.put("username", user != null ? user.getUsername() : "Unknown");
                    map.put("volume", entry.getValue());
                    return map;
                })
                .toList();
    }
}
