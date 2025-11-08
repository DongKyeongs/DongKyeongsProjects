package com.dkp.exchange.controller;

import com.dkp.exchange.model.*;
import com.dkp.exchange.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private final AdminService adminService;
    private final KycService kycService;
    private final AmlService amlService;
    private final VipService vipService;
    private final FeeAnalyticsService feeAnalyticsService;
    private final EnhancedFeeService enhancedFeeService;

    // ==================== Dashboard ====================

    @GetMapping("/dashboard/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        Map<String, Object> stats = adminService.getDashboardStats();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/dashboard/recent-trades")
    public ResponseEntity<List<Map<String, Object>>> getRecentTrades(
            @RequestParam(defaultValue = "20") int limit) {
        List<Map<String, Object>> trades = adminService.getRecentTrades(limit);
        return ResponseEntity.ok(trades);
    }

    @GetMapping("/dashboard/recent-users")
    public ResponseEntity<List<Map<String, Object>>> getRecentUsers(
            @RequestParam(defaultValue = "10") int limit) {
        List<Map<String, Object>> users = adminService.getRecentUsers(limit);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/dashboard/volume-by-symbol")
    public ResponseEntity<Map<String, Object>> getVolumeBySymbol() {
        Map<String, Object> volume = Map.of("volumes", adminService.getTradingVolumeBySymbol());
        return ResponseEntity.ok(volume);
    }

    @GetMapping("/dashboard/top-traders")
    public ResponseEntity<List<Map<String, Object>>> getTopTraders(
            @RequestParam(defaultValue = "10") int limit) {
        List<Map<String, Object>> traders = adminService.getTopTraders(limit);
        return ResponseEntity.ok(traders);
    }

    // ==================== User Management ====================

    @GetMapping("/users")
    public ResponseEntity<Page<User>> getAllUsers(Pageable pageable) {
        Page<User> users = adminService.getAllUsers(pageable);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/search")
    public ResponseEntity<List<User>> searchUsers(@RequestParam String keyword) {
        List<User> users = adminService.searchUsers(keyword);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<User> getUserDetails(@PathVariable Long userId) {
        User user = adminService.getUserDetails(userId);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/users/{userId}/toggle-enabled")
    public ResponseEntity<Map<String, String>> toggleUserEnabled(@PathVariable Long userId) {
        adminService.toggleUserEnabled(userId);
        return ResponseEntity.ok(Map.of("message", "User enabled status toggled successfully"));
    }

    @PostMapping("/users/{userId}/set-trading")
    public ResponseEntity<Map<String, String>> setTradingEnabled(
            @PathVariable Long userId,
            @RequestParam boolean enabled) {
        adminService.setTradingEnabled(userId, enabled);
        return ResponseEntity.ok(Map.of("message", "Trading status updated successfully"));
    }

    @PostMapping("/users/{userId}/set-withdrawal")
    public ResponseEntity<Map<String, String>> setWithdrawalEnabled(
            @PathVariable Long userId,
            @RequestParam boolean enabled) {
        adminService.setWithdrawalEnabled(userId, enabled);
        return ResponseEntity.ok(Map.of("message", "Withdrawal status updated successfully"));
    }

    // ==================== Fee Management ====================

    @GetMapping("/fees")
    public ResponseEntity<List<Fee>> getAllFees() {
        List<Fee> fees = adminService.getAllFees();
        return ResponseEntity.ok(fees);
    }

    @GetMapping("/fees/symbol/{symbol}")
    public ResponseEntity<List<Fee>> getFeesBySymbol(@PathVariable String symbol) {
        List<Fee> fees = adminService.getFeesBySymbol(symbol);
        return ResponseEntity.ok(fees);
    }

    @PostMapping("/fees")
    public ResponseEntity<Fee> createFee(@RequestBody Fee fee) {
        Fee created = adminService.createFee(fee);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/fees/{feeId}")
    public ResponseEntity<Fee> updateFee(
            @PathVariable Long feeId,
            @RequestBody Fee fee) {
        Fee updated = adminService.updateFee(feeId, fee);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/fees/{feeId}")
    public ResponseEntity<Map<String, String>> deleteFee(@PathVariable Long feeId) {
        adminService.deleteFee(feeId);
        return ResponseEntity.ok(Map.of("message", "Fee deleted successfully"));
    }

    @PostMapping("/fees/{feeId}/toggle-active")
    public ResponseEntity<Map<String, String>> toggleFeeActive(@PathVariable Long feeId) {
        adminService.toggleFeeActive(feeId);
        return ResponseEntity.ok(Map.of("message", "Fee active status toggled successfully"));
    }

    // ==================== KYC Management ====================

    @GetMapping("/kyc/pending")
    public ResponseEntity<List<KycVerification>> getPendingKyc() {
        List<KycVerification> pending = adminService.getPendingKycVerifications();
        return ResponseEntity.ok(pending);
    }

    @GetMapping("/kyc/all")
    public ResponseEntity<List<KycVerification>> getAllKyc() {
        List<KycVerification> all = adminService.getAllKycVerifications();
        return ResponseEntity.ok(all);
    }

    @GetMapping("/kyc/{kycId}")
    public ResponseEntity<KycVerification> getKycDetails(@PathVariable Long kycId) {
        KycVerification kyc = adminService.getKycVerification(kycId);
        return ResponseEntity.ok(kyc);
    }

    @PostMapping("/kyc/{kycId}/approve")
    public ResponseEntity<Map<String, String>> approveKyc(
            @PathVariable Long kycId,
            @RequestParam KycVerification.KycLevel level) {
        kycService.approveKyc(kycId, level);
        return ResponseEntity.ok(Map.of("message", "KYC approved successfully"));
    }

    @PostMapping("/kyc/{kycId}/reject")
    public ResponseEntity<Map<String, String>> rejectKyc(
            @PathVariable Long kycId,
            @RequestParam String reason) {
        kycService.rejectKyc(kycId, reason);
        return ResponseEntity.ok(Map.of("message", "KYC rejected successfully"));
    }

    // ==================== AML Management ====================

    @GetMapping("/aml/open")
    public ResponseEntity<List<AmlAlert>> getOpenAmlAlerts() {
        List<AmlAlert> alerts = adminService.getOpenAmlAlerts();
        return ResponseEntity.ok(alerts);
    }

    @GetMapping("/aml/all")
    public ResponseEntity<List<AmlAlert>> getAllAmlAlerts() {
        List<AmlAlert> alerts = adminService.getAllAmlAlerts();
        return ResponseEntity.ok(alerts);
    }

    @PostMapping("/aml/{alertId}/update-status")
    public ResponseEntity<Map<String, String>> updateAmlAlertStatus(
            @PathVariable Long alertId,
            @RequestParam AmlAlert.AlertStatus status,
            @RequestParam(required = false) String notes) {
        adminService.updateAmlAlertStatus(alertId, status, notes);
        return ResponseEntity.ok(Map.of("message", "AML alert status updated successfully"));
    }

    // ==================== System Settings ====================

    @GetMapping("/settings")
    public ResponseEntity<Map<String, String>> getSystemSettings() {
        // Placeholder for system settings
        Map<String, String> settings = Map.of(
                "maintenance", "false",
                "registrationEnabled", "true",
                "tradingEnabled", "true"
        );
        return ResponseEntity.ok(settings);
    }

    @PostMapping("/settings")
    public ResponseEntity<Map<String, String>> updateSystemSettings(
            @RequestBody Map<String, String> settings) {
        // Placeholder for updating system settings
        log.info("System settings updated: {}", settings);
        return ResponseEntity.ok(Map.of("message", "Settings updated successfully"));
    }

    // ==================== VIP Management ====================

    @GetMapping("/vip/user/{userId}")
    public ResponseEntity<VipService.VipInfo> getUserVipInfo(@PathVariable Long userId) {
        VipService.VipInfo vipInfo = vipService.getVipInfo(userId);
        return ResponseEntity.ok(vipInfo);
    }

    @PostMapping("/vip/user/{userId}/update")
    public ResponseEntity<Map<String, Object>> updateUserVipLevel(@PathVariable Long userId) {
        VipLevel newLevel = vipService.updateUserVipLevel(userId);
        return ResponseEntity.ok(Map.of(
                "message", "VIP level updated successfully",
                "newLevel", newLevel
        ));
    }

    @PostMapping("/vip/user/{userId}/set-level")
    public ResponseEntity<Map<String, String>> setUserVipLevel(
            @PathVariable Long userId,
            @RequestParam VipLevel level) {
        vipService.setUserVipLevel(userId, level);
        return ResponseEntity.ok(Map.of("message", "VIP level set successfully"));
    }

    @PostMapping("/vip/update-all")
    public ResponseEntity<Map<String, String>> updateAllVipLevels() {
        vipService.updateAllUsersVipLevels();
        return ResponseEntity.ok(Map.of("message", "All VIP levels updated successfully"));
    }

    @GetMapping("/vip/stats")
    public ResponseEntity<List<FeeAnalyticsService.VipFeeStats>> getVipStats() {
        List<FeeAnalyticsService.VipFeeStats> stats = feeAnalyticsService.getVipFeeStats();
        return ResponseEntity.ok(stats);
    }

    // ==================== Fee Analytics ====================

    @GetMapping("/fee-analytics/overall")
    public ResponseEntity<FeeAnalyticsService.OverallFeeStats> getOverallFeeStats() {
        FeeAnalyticsService.OverallFeeStats stats = feeAnalyticsService.getOverallFeeStats();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/fee-analytics/user/{userId}")
    public ResponseEntity<FeeAnalyticsService.UserFeeStats> getUserFeeStats(@PathVariable Long userId) {
        FeeAnalyticsService.UserFeeStats stats = feeAnalyticsService.getUserFeeStats(userId);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/fee-analytics/by-symbol")
    public ResponseEntity<List<FeeAnalyticsService.SymbolFeeStats>> getSymbolFeeStats() {
        List<FeeAnalyticsService.SymbolFeeStats> stats = feeAnalyticsService.getSymbolFeeStats();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/fee-analytics/by-vip")
    public ResponseEntity<List<FeeAnalyticsService.VipFeeStats>> getFeeStatsByVip() {
        List<FeeAnalyticsService.VipFeeStats> stats = feeAnalyticsService.getVipFeeStats();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/fee-analytics/daily")
    public ResponseEntity<List<FeeAnalyticsService.DailyFeeStats>> getDailyFeeStats(
            @RequestParam(defaultValue = "30") int days) {
        List<FeeAnalyticsService.DailyFeeStats> stats = feeAnalyticsService.getDailyFeeStats(days);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/fee-analytics/discount-info/{userId}")
    public ResponseEntity<EnhancedFeeService.FeeDiscountInfo> getFeeDiscountInfo(@PathVariable Long userId) {
        com.dkp.exchange.model.User user = adminService.getUserDetails(userId);
        EnhancedFeeService.FeeDiscountInfo info = enhancedFeeService.getFeeDiscountInfo(user);
        return ResponseEntity.ok(info);
    }
}
