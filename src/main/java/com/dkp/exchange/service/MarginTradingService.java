package com.dkp.exchange.service;

import com.dkp.exchange.model.*;
import com.dkp.exchange.repository.MarginAccountRepository;
import com.dkp.exchange.repository.PositionRepository;
import com.dkp.exchange.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MarginTradingService {
    private final MarginAccountRepository marginAccountRepository;
    private final PositionRepository positionRepository;
    private final UserRepository userRepository;
    private final PriceService priceService;

    // 마진 계정 생성
    @Transactional
    public MarginAccount createMarginAccount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (marginAccountRepository.findByUserId(userId).isPresent()) {
            throw new RuntimeException("Margin account already exists");
        }

        MarginAccount account = new MarginAccount();
        account.setUser(user);
        account.setMaxLeverage(10); // 기본 10x
        account.setStatus(MarginAccount.MarginAccountStatus.ACTIVE);

        return marginAccountRepository.save(account);
    }

    // 롱 포지션 오픈
    @Transactional
    public Position openLongPosition(Long userId, String symbol, BigDecimal quantity,
                                      BigDecimal price, Integer leverage) {
        MarginAccount account = getMarginAccount(userId);
        validateLeverage(leverage, account.getMaxLeverage());

        // 필요 증거금 계산
        BigDecimal positionValue = quantity.multiply(price);
        BigDecimal requiredMargin = positionValue.divide(new BigDecimal(leverage), 8, RoundingMode.HALF_UP);

        if (account.getAvailableBalance().compareTo(requiredMargin) < 0) {
            throw new RuntimeException("Insufficient margin");
        }

        // 빌린 금액 계산
        BigDecimal borrowedAmount = positionValue.subtract(requiredMargin);

        // 청산 가격 계산 (마진 레벨 120%에서 청산)
        BigDecimal liquidationPrice = calculateLiquidationPrice(
                price, requiredMargin, borrowedAmount, Position.PositionSide.LONG);

        // 포지션 생성
        Position position = new Position();
        position.setMarginAccount(account);
        position.setSymbol(symbol);
        position.setSide(Position.PositionSide.LONG);
        position.setQuantity(quantity);
        position.setEntryPrice(price);
        position.setLiquidationPrice(liquidationPrice);
        position.setLeverage(leverage);
        position.setMargin(requiredMargin);
        position.setStatus(Position.PositionStatus.OPEN);

        // 마진 계정 업데이트
        account.setAvailableBalance(account.getAvailableBalance().subtract(requiredMargin));
        account.setBorrowedAmount(account.getBorrowedAmount().add(borrowedAmount));

        marginAccountRepository.save(account);
        Position saved = positionRepository.save(position);

        log.info("Opened LONG position: {} {} at {} with {}x leverage",
                quantity, symbol, price, leverage);

        return saved;
    }

    // 숏 포지션 오픈
    @Transactional
    public Position openShortPosition(Long userId, String symbol, BigDecimal quantity,
                                       BigDecimal price, Integer leverage) {
        MarginAccount account = getMarginAccount(userId);
        validateLeverage(leverage, account.getMaxLeverage());

        BigDecimal positionValue = quantity.multiply(price);
        BigDecimal requiredMargin = positionValue.divide(new BigDecimal(leverage), 8, RoundingMode.HALF_UP);

        if (account.getAvailableBalance().compareTo(requiredMargin) < 0) {
            throw new RuntimeException("Insufficient margin");
        }

        BigDecimal borrowedAmount = positionValue.subtract(requiredMargin);

        BigDecimal liquidationPrice = calculateLiquidationPrice(
                price, requiredMargin, borrowedAmount, Position.PositionSide.SHORT);

        Position position = new Position();
        position.setMarginAccount(account);
        position.setSymbol(symbol);
        position.setSide(Position.PositionSide.SHORT);
        position.setQuantity(quantity);
        position.setEntryPrice(price);
        position.setLiquidationPrice(liquidationPrice);
        position.setLeverage(leverage);
        position.setMargin(requiredMargin);
        position.setStatus(Position.PositionStatus.OPEN);

        account.setAvailableBalance(account.getAvailableBalance().subtract(requiredMargin));
        account.setBorrowedAmount(account.getBorrowedAmount().add(borrowedAmount));

        marginAccountRepository.save(account);
        Position saved = positionRepository.save(position);

        log.info("Opened SHORT position: {} {} at {} with {}x leverage",
                quantity, symbol, price, leverage);

        return saved;
    }

    // 포지션 종료
    @Transactional
    public void closePosition(Long positionId, BigDecimal closePrice) {
        Position position = positionRepository.findById(positionId)
                .orElseThrow(() -> new RuntimeException("Position not found"));

        if (position.getStatus() != Position.PositionStatus.OPEN) {
            throw new RuntimeException("Position is not open");
        }

        // 손익 계산
        BigDecimal pnl = position.calculateUnrealizedPnL(closePrice);
        position.setRealizedPnL(pnl);
        position.setStatus(Position.PositionStatus.CLOSED);
        position.setClosedAt(LocalDateTime.now());

        // 마진 계정 업데이트
        MarginAccount account = position.getMarginAccount();
        BigDecimal returnAmount = position.getMargin().add(pnl);

        account.setAvailableBalance(account.getAvailableBalance().add(returnAmount));
        account.setTotalBalance(account.getTotalBalance().add(pnl));

        // 빌린 금액 상환
        BigDecimal positionValue = position.getQuantity().multiply(position.getEntryPrice());
        BigDecimal borrowedAmount = positionValue.subtract(position.getMargin());
        account.setBorrowedAmount(account.getBorrowedAmount().subtract(borrowedAmount));

        marginAccountRepository.save(account);
        positionRepository.save(position);

        log.info("Closed position {} with PnL: {}", positionId, pnl);
    }

    // 강제 청산 모니터링
    @Scheduled(fixedDelay = 5000) // 5초마다 체크
    @Transactional
    public void monitorLiquidations() {
        List<Position> openPositions = positionRepository.findAllOpenPositions();

        for (Position position : openPositions) {
            try {
                BigDecimal currentPrice = priceService.getCurrentPrice(position.getSymbol());

                // 청산 가격 도달 체크
                if (shouldLiquidate(position, currentPrice)) {
                    liquidatePosition(position, currentPrice);
                } else {
                    // 미실현 손익 업데이트
                    BigDecimal unrealizedPnL = position.calculateUnrealizedPnL(currentPrice);
                    position.setUnrealizedPnL(unrealizedPnL);
                    positionRepository.save(position);
                }
            } catch (Exception e) {
                log.error("Error monitoring position {}: {}", position.getId(), e.getMessage());
            }
        }
    }

    private boolean shouldLiquidate(Position position, BigDecimal currentPrice) {
        if (position.getSide() == Position.PositionSide.LONG) {
            return currentPrice.compareTo(position.getLiquidationPrice()) <= 0;
        } else {
            return currentPrice.compareTo(position.getLiquidationPrice()) >= 0;
        }
    }

    private void liquidatePosition(Position position, BigDecimal liquidationPrice) {
        position.setStatus(Position.PositionStatus.LIQUIDATED);
        position.setClosedAt(LocalDateTime.now());

        BigDecimal pnl = position.calculateUnrealizedPnL(liquidationPrice);
        position.setRealizedPnL(pnl);

        // 마진 손실 처리
        MarginAccount account = position.getMarginAccount();
        account.setTotalBalance(account.getTotalBalance().add(pnl));

        // 빌린 금액 상환 (청산으로 모두 상실)
        BigDecimal positionValue = position.getQuantity().multiply(position.getEntryPrice());
        BigDecimal borrowedAmount = positionValue.subtract(position.getMargin());
        account.setBorrowedAmount(account.getBorrowedAmount().subtract(borrowedAmount));

        marginAccountRepository.save(account);
        positionRepository.save(position);

        log.warn("LIQUIDATED position {} at price {}", position.getId(), liquidationPrice);
    }

    private BigDecimal calculateLiquidationPrice(BigDecimal entryPrice, BigDecimal margin,
                                                   BigDecimal borrowed, Position.PositionSide side) {
        // 간단한 청산가 계산 (실제로는 더 복잡)
        BigDecimal maintenanceMarginRate = new BigDecimal("0.05"); // 5% 유지 증거금

        if (side == Position.PositionSide.LONG) {
            // LONG 청산가 = 진입가 * (1 - 마진 / 포지션가치)
            BigDecimal positionValue = borrowed.add(margin);
            BigDecimal ratio = margin.divide(positionValue, 8, RoundingMode.HALF_UP);
            return entryPrice.multiply(BigDecimal.ONE.subtract(ratio).add(maintenanceMarginRate));
        } else {
            // SHORT 청산가 = 진입가 * (1 + 마진 / 포지션가치)
            BigDecimal positionValue = borrowed.add(margin);
            BigDecimal ratio = margin.divide(positionValue, 8, RoundingMode.HALF_UP);
            return entryPrice.multiply(BigDecimal.ONE.add(ratio).subtract(maintenanceMarginRate));
        }
    }

    private void validateLeverage(Integer leverage, Integer maxLeverage) {
        if (leverage < 1 || leverage > maxLeverage) {
            throw new RuntimeException("Invalid leverage. Must be between 1 and " + maxLeverage);
        }
    }

    private MarginAccount getMarginAccount(Long userId) {
        return marginAccountRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Margin account not found"));
    }

    // 마진 입금
    @Transactional
    public void depositMargin(Long userId, BigDecimal amount) {
        MarginAccount account = getMarginAccount(userId);
        account.setTotalBalance(account.getTotalBalance().add(amount));
        account.setAvailableBalance(account.getAvailableBalance().add(amount));
        marginAccountRepository.save(account);

        log.info("Margin deposited: {} for user {}", amount, userId);
    }

    // 마진 출금
    @Transactional
    public void withdrawMargin(Long userId, BigDecimal amount) {
        MarginAccount account = getMarginAccount(userId);

        if (account.getAvailableBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient available balance");
        }

        account.setTotalBalance(account.getTotalBalance().subtract(amount));
        account.setAvailableBalance(account.getAvailableBalance().subtract(amount));
        marginAccountRepository.save(account);

        log.info("Margin withdrawn: {} for user {}", amount, userId);
    }
}
