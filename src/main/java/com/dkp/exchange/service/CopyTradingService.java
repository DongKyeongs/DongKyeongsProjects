package com.dkp.exchange.service;

import com.dkp.exchange.model.*;
import com.dkp.exchange.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 카피 트레이딩 핵심 서비스
 * 시그널 제공자의 거래를 팔로워에게 자동 복사
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CopyTradingService {
    private final CopyTradeRepository copyTradeRepository;
    private final CopyTradeExecutionRepository executionRepository;
    private final SignalProviderRepository providerRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    /**
     * 카피 트레이드 시작
     */
    @Transactional
    public CopyTrade startCopyTrade(Long followerId, Long providerId, CopyTrade settings) {
        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new RuntimeException("Follower not found"));

        SignalProvider provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new RuntimeException("Provider not found"));

        // 제공자가 팔로워를 받을 수 있는지 확인
        if (!provider.canAcceptFollower()) {
            throw new RuntimeException("Provider is not accepting followers");
        }

        // CopyTrade 생성
        CopyTrade copyTrade = new CopyTrade();
        copyTrade.setFollower(follower);
        copyTrade.setProvider(provider);
        copyTrade.setStatus(CopyTrade.CopyStatus.ACTIVE);
        copyTrade.setMode(settings.getMode());
        copyTrade.setCopyAmount(settings.getCopyAmount());
        copyTrade.setCopyRatio(settings.getCopyRatio());
        copyTrade.setMaxPerTrade(settings.getMaxPerTrade());
        copyTrade.setMinPerTrade(settings.getMinPerTrade());
        copyTrade.setStopLossPercent(settings.getStopLossPercent());
        copyTrade.setTakeProfitPercent(settings.getTakeProfitPercent());
        copyTrade.setSymbolFilter(settings.getSymbolFilter());
        copyTrade.setCopyMarketOrders(settings.isCopyMarketOrders());
        copyTrade.setCopyLimitOrders(settings.isCopyLimitOrders());
        copyTrade.setCopyStopOrders(settings.isCopyStopOrders());

        CopyTrade saved = copyTradeRepository.save(copyTrade);

        // 제공자 팔로워 수 증가
        provider.setFollowerCount(provider.getFollowerCount() + 1);
        providerRepository.save(provider);

        log.info("Copy trade started: follower={}, provider={}", followerId, providerId);
        return saved;
    }

    /**
     * 카피 트레이드 중지
     */
    @Transactional
    public void stopCopyTrade(Long copyTradeId) {
        CopyTrade copyTrade = copyTradeRepository.findById(copyTradeId)
                .orElseThrow(() -> new RuntimeException("Copy trade not found"));

        copyTrade.setStatus(CopyTrade.CopyStatus.STOPPED);
        copyTrade.setStoppedAt(LocalDateTime.now());
        copyTradeRepository.save(copyTrade);

        // 제공자 팔로워 수 감소
        SignalProvider provider = copyTrade.getProvider();
        provider.setFollowerCount(Math.max(0, provider.getFollowerCount() - 1));
        providerRepository.save(provider);

        log.info("Copy trade stopped: id={}", copyTradeId);
    }

    /**
     * 제공자의 거래를 팔로워들에게 복사 (핵심 로직)
     */
    @Transactional
    public void copyTradeToFollowers(Long providerOrderId) {
        Order providerOrder = orderRepository.findById(providerOrderId)
                .orElseThrow(() -> new RuntimeException("Provider order not found"));

        SignalProvider provider = providerRepository.findByUserId(providerOrder.getUser().getId())
                .orElse(null);

        if (provider == null) {
            return; // 시그널 제공자가 아니면 복사하지 않음
        }

        // 활성 팔로워 조회
        List<CopyTrade> activeCopyTrades = copyTradeRepository.findActiveByProviderId(provider.getId());

        log.info("Copying trade to {} followers: provider={}, order={}",
                activeCopyTrades.size(), provider.getId(), providerOrderId);

        for (CopyTrade copyTrade : activeCopyTrades) {
            try {
                executeCopyTrade(copyTrade, providerOrder);
            } catch (Exception e) {
                log.error("Failed to copy trade for follower {}: {}",
                        copyTrade.getFollower().getId(), e.getMessage());
            }
        }
    }

    /**
     * 개별 팔로워에게 거래 복사 실행
     */
    private void executeCopyTrade(CopyTrade copyTrade, Order providerOrder) {
        // 필터 체크
        if (!copyTrade.isSymbolAllowed(providerOrder.getSymbol())) {
            log.debug("Symbol not allowed: {}", providerOrder.getSymbol());
            return;
        }

        if (!copyTrade.isOrderTypeAllowed(providerOrder.getType())) {
            log.debug("Order type not allowed: {}", providerOrder.getType());
            return;
        }

        // 복사 금액 계산
        BigDecimal providerAmount = providerOrder.getAmount();
        BigDecimal copyAmount = copyTrade.calculateCopyAmount(providerAmount);

        // 잔고 확인 (간단 버전 - 실제로는 더 복잡)
        User follower = copyTrade.getFollower();
        // TODO: 실제 잔고 확인 로직

        // 복사 주문 생성
        Order followerOrder = new Order();
        followerOrder.setUser(follower);
        followerOrder.setSymbol(providerOrder.getSymbol());
        followerOrder.setType(providerOrder.getType());
        followerOrder.setSide(providerOrder.getSide());
        followerOrder.setAmount(copyAmount);
        followerOrder.setPrice(providerOrder.getPrice());
        followerOrder.setStatus(OrderStatus.PENDING);
        followerOrder.setCreatedAt(LocalDateTime.now());

        Order savedOrder = orderRepository.save(followerOrder);

        // 실행 기록 생성
        CopyTradeExecution execution = new CopyTradeExecution();
        execution.setCopyTrade(copyTrade);
        execution.setProviderOrder(providerOrder);
        execution.setFollowerOrder(savedOrder);
        execution.setStatus(CopyTradeExecution.ExecutionStatus.COMPLETED);
        execution.setSymbol(providerOrder.getSymbol());
        execution.setOrderType(providerOrder.getType());
        execution.setSide(providerOrder.getSide());
        execution.setProviderAmount(providerAmount);
        execution.setFollowerAmount(copyAmount);
        execution.setProviderPrice(providerOrder.getPrice());
        execution.setFollowerPrice(savedOrder.getPrice());
        execution.setCopyRatio(copyTrade.getCopyRatio());
        execution.setExecutedAt(LocalDateTime.now());
        execution.setCompletedAt(LocalDateTime.now());

        executionRepository.save(execution);

        // 통계 업데이트
        copyTrade.setCopiedTrades(copyTrade.getCopiedTrades() + 1);
        copyTrade.setLastCopiedAt(LocalDateTime.now());
        copyTradeRepository.save(copyTrade);

        log.info("Trade copied: follower={}, amount={}", follower.getId(), copyAmount);
    }

    /**
     * 팔로워의 활성 복사 거래 조회
     */
    @Transactional(readOnly = true)
    public List<CopyTrade> getFollowerCopyTrades(Long followerId) {
        return copyTradeRepository.findByFollowerId(followerId);
    }

    /**
     * 복사 거래 실행 내역 조회
     */
    @Transactional(readOnly = true)
    public List<CopyTradeExecution> getCopyTradeExecutions(Long copyTradeId) {
        return executionRepository.findByCopyTradeId(copyTradeId);
    }
}
