package com.dkp.exchange.service;

import com.dkp.exchange.model.*;
import com.dkp.exchange.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 시그널 제공자 관리 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SignalProviderService {
    private final SignalProviderRepository providerRepository;
    private final SignalProviderPerformanceRepository performanceRepository;
    private final CopyTradeRepository copyTradeRepository;
    private final UserRepository userRepository;

    /**
     * 시그널 제공자 등록
     */
    @Transactional
    public SignalProvider registerAsProvider(Long userId, SignalProvider providerData) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 이미 제공자인지 확인
        if (providerRepository.findByUserId(userId).isPresent()) {
            throw new RuntimeException("User is already a signal provider");
        }

        SignalProvider provider = new SignalProvider();
        provider.setUser(user);
        provider.setNickname(providerData.getNickname());
        provider.setDescription(providerData.getDescription());
        provider.setStrategy(providerData.getStrategy());
        provider.setStatus(SignalProvider.ProviderStatus.ACTIVE);
        provider.setPerformanceFeePercent(providerData.getPerformanceFeePercent());
        provider.setMinCopyAmount(providerData.getMinCopyAmount());
        provider.setMaxCopyAmount(providerData.getMaxCopyAmount());
        provider.setMaxFollowers(providerData.getMaxFollowers());

        SignalProvider saved = providerRepository.save(provider);
        log.info("User {} registered as signal provider", userId);
        return saved;
    }

    /**
     * 활성 제공자 목록 조회
     */
    @Transactional(readOnly = true)
    public List<SignalProvider> getActiveProviders() {
        return providerRepository.findActiveProviders();
    }

    /**
     * 상위 성과 제공자 조회
     */
    @Transactional(readOnly = true)
    public List<SignalProvider> getTopPerformers(int limit) {
        return providerRepository.findTopPerformers().stream()
                .limit(limit)
                .toList();
    }

    /**
     * 인기 제공자 조회 (팔로워 수 기준)
     */
    @Transactional(readOnly = true)
    public List<SignalProvider> getMostFollowed(int limit) {
        return providerRepository.findMostFollowed().stream()
                .limit(limit)
                .toList();
    }

    /**
     * 제공자 검색
     */
    @Transactional(readOnly = true)
    public List<SignalProvider> searchProviders(String keyword) {
        return providerRepository.searchByNickname(keyword);
    }

    /**
     * 제공자 상세 정보 조회
     */
    @Transactional(readOnly = true)
    public SignalProvider getProviderDetails(Long providerId) {
        return providerRepository.findById(providerId)
                .orElseThrow(() -> new RuntimeException("Provider not found"));
    }

    /**
     * 제공자 성과 내역 조회
     */
    @Transactional(readOnly = true)
    public List<SignalProviderPerformance> getProviderPerformance(Long providerId, int days) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days);
        return performanceRepository.findByProviderIdBetweenDates(providerId, startDate, endDate);
    }

    /**
     * 제공자 설정 업데이트
     */
    @Transactional
    public void updateProviderSettings(Long providerId, SignalProvider updates) {
        SignalProvider provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new RuntimeException("Provider not found"));

        if (updates.getDescription() != null) {
            provider.setDescription(updates.getDescription());
        }
        if (updates.getStrategy() != null) {
            provider.setStrategy(updates.getStrategy());
        }
        if (updates.getPerformanceFeePercent() != null) {
            provider.setPerformanceFeePercent(updates.getPerformanceFeePercent());
        }
        if (updates.getMinCopyAmount() != null) {
            provider.setMinCopyAmount(updates.getMinCopyAmount());
        }
        if (updates.getMaxCopyAmount() != null) {
            provider.setMaxCopyAmount(updates.getMaxCopyAmount());
        }

        provider.setAcceptingFollowers(updates.isAcceptingFollowers());
        providerRepository.save(provider);

        log.info("Provider settings updated: id={}", providerId);
    }

    /**
     * 제공자 비활성화
     */
    @Transactional
    public void deactivateProvider(Long providerId) {
        SignalProvider provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new RuntimeException("Provider not found"));

        provider.setStatus(SignalProvider.ProviderStatus.INACTIVE);
        provider.setAcceptingFollowers(false);
        providerRepository.save(provider);

        // 모든 활성 복사 중지
        List<CopyTrade> activeCopies = copyTradeRepository.findActiveByProviderId(providerId);
        for (CopyTrade copy : activeCopies) {
            copy.setStatus(CopyTrade.CopyStatus.STOPPED);
            copyTradeRepository.save(copy);
        }

        log.info("Provider deactivated: id={}", providerId);
    }

    /**
     * 랭킹 업데이트
     */
    @Transactional
    public void updateRankings() {
        List<SignalProvider> providers = providerRepository.findByStatus(SignalProvider.ProviderStatus.ACTIVE);

        // 랭킹 점수 계산 (수익률 + 팔로워 수 + 거래량 등을 고려)
        for (SignalProvider provider : providers) {
            BigDecimal score = calculateRankingScore(provider);
            provider.setRankingScore(score);
        }

        // 점수순으로 정렬하여 랭킹 부여
        providers.stream()
                .sorted((p1, p2) -> p2.getRankingScore().compareTo(p1.getRankingScore()))
                .forEachOrdered(provider -> {
                    int ranking = providers.indexOf(provider) + 1;
                    provider.setRanking(ranking);
                    providerRepository.save(provider);
                });

        log.info("Rankings updated for {} providers", providers.size());
    }

    /**
     * 랭킹 점수 계산
     */
    private BigDecimal calculateRankingScore(SignalProvider provider) {
        BigDecimal returnScore = provider.getTotalReturnPercent().multiply(new BigDecimal("10"));
        BigDecimal followerScore = new BigDecimal(provider.getFollowerCount());
        BigDecimal volumeScore = provider.getTotalVolume().divide(new BigDecimal("1000"), 2, java.math.RoundingMode.HALF_UP);
        BigDecimal winRateScore = provider.getWinRate().multiply(new BigDecimal("5"));

        return returnScore.add(followerScore).add(volumeScore).add(winRateScore);
    }
}
