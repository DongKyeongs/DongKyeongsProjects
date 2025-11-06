package com.dkp.exchange.service;

import com.dkp.exchange.model.StakingPosition;
import com.dkp.exchange.model.StakingProduct;
import com.dkp.exchange.model.User;
import com.dkp.exchange.model.Wallet;
import com.dkp.exchange.repository.StakingPositionRepository;
import com.dkp.exchange.repository.StakingProductRepository;
import com.dkp.exchange.repository.UserRepository;
import com.dkp.exchange.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StakingService {
    private final StakingProductRepository stakingProductRepository;
    private final StakingPositionRepository stakingPositionRepository;
    private final WalletRepository walletRepository;
    private final UserRepository userRepository;

    // 스테이킹 시작
    @Transactional
    public StakingPosition stake(Long userId, Long productId, BigDecimal amount) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        StakingProduct product = stakingProductRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // 검증
        if (!product.isActive()) {
            throw new RuntimeException("Product is not active");
        }

        if (amount.compareTo(product.getMinAmount()) < 0) {
            throw new RuntimeException("Amount below minimum");
        }

        if (product.getMaxAmount() != null && amount.compareTo(product.getMaxAmount()) > 0) {
            throw new RuntimeException("Amount exceeds maximum");
        }

        if (!product.hasCapacityFor(amount)) {
            throw new RuntimeException("Product capacity exceeded");
        }

        // 지갑에서 차감
        Wallet wallet = walletRepository.findByUserIdAndAssetForUpdate(userId, product.getAsset())
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        if (wallet.getAvailableBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        wallet.lock(amount);
        walletRepository.save(wallet);

        // 스테이킹 포지션 생성
        StakingPosition position = new StakingPosition();
        position.setUser(user);
        position.setProduct(product);
        position.setStakedAmount(amount);
        position.setStatus(StakingPosition.StakingPositionStatus.ACTIVE);

        // 상품의 총 스테이킹 양 업데이트
        product.setTotalStaked(product.getTotalStaked().add(amount));
        stakingProductRepository.save(product);

        StakingPosition saved = stakingPositionRepository.save(position);
        log.info("Staked {} {} for user {} in product {}", amount, product.getAsset(), userId, product.getName());

        return saved;
    }

    // 스테이킹 해제
    @Transactional
    public void unstake(Long positionId, Long userId) {
        StakingPosition position = stakingPositionRepository.findById(positionId)
                .orElseThrow(() -> new RuntimeException("Position not found"));

        if (!position.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        if (!position.canUnstake()) {
            throw new RuntimeException("Cannot unstake yet. Position is locked until " + position.getMaturityDate());
        }

        // 최종 보상 계산
        calculateRewards(position);

        // 원금 + 보상 반환
        BigDecimal totalAmount = position.getStakedAmount().add(position.getEarnedRewards()).add(position.getPendingRewards());

        Wallet wallet = walletRepository.findByUserIdAndAssetForUpdate(userId, position.getProduct().getAsset())
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        wallet.unlock(position.getStakedAmount());
        wallet.credit(position.getEarnedRewards().add(position.getPendingRewards())); // 보상만 추가
        walletRepository.save(wallet);

        // 상태 업데이트
        position.setStatus(StakingPosition.StakingPositionStatus.UNSTAKED);
        position.setUnstakedAt(LocalDateTime.now());
        stakingPositionRepository.save(position);

        // 상품의 총 스테이킹 양 업데이트
        StakingProduct product = position.getProduct();
        product.setTotalStaked(product.getTotalStaked().subtract(position.getStakedAmount()));
        stakingProductRepository.save(product);

        log.info("Unstaked position {} with total amount: {}", positionId, totalAmount);
    }

    // 보상 계산 스케줄러 (매일 자정)
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void calculateAllRewards() {
        List<StakingPosition> activePositions = stakingPositionRepository.findAllActive();

        for (StakingPosition position : activePositions) {
            try {
                calculateRewards(position);
            } catch (Exception e) {
                log.error("Error calculating rewards for position {}: {}", position.getId(), e.getMessage());
            }
        }

        log.info("Calculated rewards for {} positions", activePositions.size());
    }

    @Transactional
    public void calculateRewards(StakingPosition position) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastCalculated = position.getLastRewardCalculatedAt();

        // 경과 일수 계산
        long daysPassed = ChronoUnit.DAYS.between(lastCalculated, now);

        if (daysPassed == 0) {
            return; // 아직 하루도 안 지남
        }

        StakingProduct product = position.getProduct();

        // 일일 보상 계산
        // 연이율을 일일 이율로 변환: APR / 365
        BigDecimal dailyRate = product.getAprRate()
                .divide(new BigDecimal("100"), 10, RoundingMode.HALF_UP)
                .divide(new BigDecimal("365"), 10, RoundingMode.HALF_UP);

        // 보상 = 스테이킹 금액 * 일일 이율 * 경과 일수
        BigDecimal reward = position.getStakedAmount()
                .multiply(dailyRate)
                .multiply(new BigDecimal(daysPassed))
                .setScale(8, RoundingMode.HALF_DOWN);

        // 보상 누적
        position.setEarnedRewards(position.getEarnedRewards().add(reward));
        position.setLastRewardCalculatedAt(now);

        stakingPositionRepository.save(position);

        log.info("Calculated reward {} for position {} ({} days)", reward, position.getId(), daysPassed);
    }

    // 스테이킹 상품 생성 (관리자용)
    @Transactional
    public StakingProduct createProduct(StakingProduct product) {
        return stakingProductRepository.save(product);
    }

    @Transactional(readOnly = true)
    public List<StakingProduct> getActiveProducts() {
        return stakingProductRepository.findByActive(true);
    }

    @Transactional(readOnly = true)
    public List<StakingPosition> getUserPositions(Long userId) {
        return stakingPositionRepository.findByUserIdAndStatus(userId, StakingPosition.StakingPositionStatus.ACTIVE);
    }

    @Transactional(readOnly = true)
    public BigDecimal getUserTotalStaked(Long userId) {
        BigDecimal total = stakingPositionRepository.getTotalStakedByUser(userId);
        return total != null ? total : BigDecimal.ZERO;
    }
}
