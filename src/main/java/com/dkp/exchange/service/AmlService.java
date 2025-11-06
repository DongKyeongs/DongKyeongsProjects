package com.dkp.exchange.service;

import com.dkp.exchange.model.AmlAlert;
import com.dkp.exchange.model.Transaction;
import com.dkp.exchange.model.User;
import com.dkp.exchange.repository.AmlAlertRepository;
import com.dkp.exchange.repository.TransactionRepository;
import com.dkp.exchange.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AmlService {
    private final AmlAlertRepository amlAlertRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    // 대규모 거래 모니터링
    private static final BigDecimal LARGE_TRANSACTION_THRESHOLD = new BigDecimal("10000");

    // 고위험 국가 리스트 (실제로는 DB나 설정 파일에서 관리)
    private static final List<String> HIGH_RISK_COUNTRIES = Arrays.asList(
            "NK", "IR", "SY", "CU" // 예시
    );

    @Scheduled(fixedDelay = 60000) // 1분마다 체크
    @Transactional
    public void monitorTransactions() {
        LocalDateTime since = LocalDateTime.now().minusMinutes(5);
        List<Transaction> recentTransactions = transactionRepository.findAll();

        for (Transaction tx : recentTransactions) {
            if (tx.getCreatedAt().isAfter(since)) {
                checkTransactionForAml(tx);
            }
        }
    }

    @Transactional
    public void checkTransactionForAml(Transaction transaction) {
        User user = transaction.getUser();

        // 1. 대규모 거래 체크
        if (transaction.getAmount().compareTo(LARGE_TRANSACTION_THRESHOLD) > 0) {
            createAlert(user, AmlAlert.AlertType.LARGE_TRANSACTION,
                    AmlAlert.RiskLevel.MEDIUM,
                    String.format("Large %s transaction: %s %s",
                            transaction.getType(), transaction.getAmount(), transaction.getAsset()),
                    transaction.getAmount(), transaction.getTxHash());
        }

        // 2. 빠른 자금 이동 체크 (24시간 내 여러 출금)
        LocalDateTime dayAgo = LocalDateTime.now().minusHours(24);
        List<Transaction> recentTx = transactionRepository.findByUserIdAndType(
                user.getId(), Transaction.TransactionType.WITHDRAWAL);

        long withdrawalCount = recentTx.stream()
                .filter(tx -> tx.getCreatedAt().isAfter(dayAgo))
                .count();

        if (withdrawalCount > 5) {
            createAlert(user, AmlAlert.AlertType.RAPID_MOVEMENT,
                    AmlAlert.RiskLevel.HIGH,
                    String.format("Multiple withdrawals in 24h: %d transactions", withdrawalCount),
                    null, null);
        }

        // 3. 소액 분할 거래 패턴 (Structuring) 체크
        BigDecimal totalAmount = recentTx.stream()
                .filter(tx -> tx.getCreatedAt().isAfter(dayAgo))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (withdrawalCount > 10 && totalAmount.compareTo(LARGE_TRANSACTION_THRESHOLD) > 0) {
            createAlert(user, AmlAlert.AlertType.STRUCTURING,
                    AmlAlert.RiskLevel.HIGH,
                    String.format("Possible structuring: %d small transactions totaling %s",
                            withdrawalCount, totalAmount),
                    totalAmount, null);
        }
    }

    @Transactional
    public void checkUserForAml(User user) {
        // 고위험 국가 체크 (KYC 데이터에서 가져와야 함)
        // 여기서는 간단한 예시
        // if (HIGH_RISK_COUNTRIES.contains(user.getCountry())) {
        //     createAlert(user, AmlAlert.AlertType.HIGH_RISK_COUNTRY, ...);
        // }

        // PEP (Politically Exposed Person) 체크
        // Sanctions List 체크
        // 이런 체크는 외부 API를 사용하는 것이 일반적
    }

    @Transactional
    public AmlAlert createAlert(User user, AmlAlert.AlertType type, AmlAlert.RiskLevel riskLevel,
                                  String description, BigDecimal amount, String relatedTxId) {
        AmlAlert alert = new AmlAlert();
        alert.setUser(user);
        alert.setType(type);
        alert.setRiskLevel(riskLevel);
        alert.setDescription(description);
        alert.setAmount(amount);
        alert.setRelatedTransactionId(relatedTxId);
        alert.setStatus(AmlAlert.AlertStatus.OPEN);

        AmlAlert saved = amlAlertRepository.save(alert);
        log.warn("AML Alert created: {} - {} for user {}", type, riskLevel, user.getId());

        // CRITICAL인 경우 사용자 계정 일시 정지 등의 조치 가능
        if (riskLevel == AmlAlert.RiskLevel.CRITICAL) {
            log.error("CRITICAL AML alert for user {}: {}", user.getId(), description);
            // TODO: 계정 정지 로직
        }

        return saved;
    }

    @Transactional
    public void resolveAlert(Long alertId, String notes) {
        AmlAlert alert = amlAlertRepository.findById(alertId)
                .orElseThrow(() -> new RuntimeException("Alert not found"));

        alert.setStatus(AmlAlert.AlertStatus.RESOLVED);
        alert.setInvestigationNotes(notes);
        alert.setResolvedAt(LocalDateTime.now());

        amlAlertRepository.save(alert);
        log.info("AML Alert {} resolved", alertId);
    }

    @Transactional
    public void markAsFalsePositive(Long alertId) {
        AmlAlert alert = amlAlertRepository.findById(alertId)
                .orElseThrow(() -> new RuntimeException("Alert not found"));

        alert.setStatus(AmlAlert.AlertStatus.FALSE_POSITIVE);
        alert.setResolvedAt(LocalDateTime.now());

        amlAlertRepository.save(alert);
        log.info("AML Alert {} marked as false positive", alertId);
    }

    @Transactional
    public void escalateAlert(Long alertId, String notes) {
        AmlAlert alert = amlAlertRepository.findById(alertId)
                .orElseThrow(() -> new RuntimeException("Alert not found"));

        alert.setStatus(AmlAlert.AlertStatus.ESCALATED);
        alert.setInvestigationNotes(notes);

        amlAlertRepository.save(alert);
        log.warn("AML Alert {} escalated to authorities", alertId);
    }

    @Transactional(readOnly = true)
    public List<AmlAlert> getOpenAlerts() {
        return amlAlertRepository.findByStatusAndRiskLevel(
                AmlAlert.AlertStatus.OPEN, AmlAlert.RiskLevel.HIGH);
    }

    @Transactional(readOnly = true)
    public long getOpenAlertCount(Long userId) {
        return amlAlertRepository.countOpenAlertsByUserId(userId);
    }
}
