package com.dkp.exchange.service;

import com.dkp.exchange.model.*;
import com.dkp.exchange.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class P2PService {
    private final P2PAdvertisementRepository advertisementRepository;
    private final P2PTradeRepository tradeRepository;
    private final WalletRepository walletRepository;
    private final UserRepository userRepository;

    // 광고 등록
    @Transactional
    public P2PAdvertisement createAdvertisement(Long userId, P2PAdvertisement ad) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 판매 광고면 에스크로에 자산 예치
        if (ad.getType() == P2PAdvertisement.TradeType.SELL) {
            Wallet wallet = walletRepository.findByUserIdAndAssetForUpdate(userId, ad.getAsset())
                    .orElseThrow(() -> new RuntimeException("Wallet not found"));

            if (wallet.getAvailableBalance().compareTo(ad.getAvailableAmount()) < 0) {
                throw new RuntimeException("Insufficient balance");
            }

            wallet.lock(ad.getAvailableAmount());
            walletRepository.save(wallet);
        }

        ad.setMerchant(user);
        ad.setStatus(P2PAdvertisement.AdvertisementStatus.ACTIVE);

        P2PAdvertisement saved = advertisementRepository.save(ad);
        log.info("P2P advertisement created: {} {} {}", ad.getType(), ad.getAmount(), ad.getAsset());

        return saved;
    }

    // 거래 시작 (에스크로)
    @Transactional
    public P2PTrade startTrade(Long advertisementId, Long buyerId, BigDecimal amount) {
        P2PAdvertisement ad = advertisementRepository.findById(advertisementId)
                .orElseThrow(() -> new RuntimeException("Advertisement not found"));

        User buyer = userRepository.findById(buyerId)
                .orElseThrow(() -> new RuntimeException("Buyer not found"));

        // 검증
        if (ad.getStatus() != P2PAdvertisement.AdvertisementStatus.ACTIVE) {
            throw new RuntimeException("Advertisement is not active");
        }

        if (amount.compareTo(ad.getMinAmount()) < 0 || amount.compareTo(ad.getMaxAmount()) > 0) {
            throw new RuntimeException("Amount out of range");
        }

        if (amount.compareTo(ad.getAvailableAmount()) > 0) {
            throw new RuntimeException("Insufficient available amount");
        }

        // 에스크로 처리
        User seller;
        BigDecimal escrowAmount;

        if (ad.getType() == P2PAdvertisement.TradeType.SELL) {
            // 광고주가 판매자
            seller = ad.getMerchant();
            escrowAmount = amount;

            // 이미 광고 등록 시 락되어 있음
        } else {
            // 광고주가 구매자, 거래 시작자가 판매자
            seller = buyer;
            buyer = ad.getMerchant();
            escrowAmount = amount;

            // 판매자의 자산 에스크로
            Wallet sellerWallet = walletRepository.findByUserIdAndAssetForUpdate(seller.getId(), ad.getAsset())
                    .orElseThrow(() -> new RuntimeException("Seller wallet not found"));

            if (sellerWallet.getAvailableBalance().compareTo(escrowAmount) < 0) {
                throw new RuntimeException("Seller has insufficient balance");
            }

            sellerWallet.lock(escrowAmount);
            walletRepository.save(sellerWallet);
        }

        // P2P 거래 생성
        P2PTrade trade = new P2PTrade();
        trade.setAdvertisement(ad);
        trade.setBuyer(buyer);
        trade.setSeller(seller);
        trade.setAmount(amount);
        trade.setPrice(ad.getPrice());
        trade.setTotalFiatAmount(amount.multiply(ad.getPrice()));
        trade.setEscrowAmount(escrowAmount);
        trade.setStatus(P2PTrade.P2PTradeStatus.PENDING_PAYMENT);

        // 광고 가능량 차감
        ad.setAvailableAmount(ad.getAvailableAmount().subtract(amount));
        advertisementRepository.save(ad);

        P2PTrade saved = tradeRepository.save(trade);
        log.info("P2P trade started: {} {} between buyer {} and seller {}",
                amount, ad.getAsset(), buyer.getId(), seller.getId());

        return saved;
    }

    // 구매자: 결제 완료 표시
    @Transactional
    public void markAsPaid(Long tradeId, Long buyerId) {
        P2PTrade trade = tradeRepository.findById(tradeId)
                .orElseThrow(() -> new RuntimeException("Trade not found"));

        if (!trade.getBuyer().getId().equals(buyerId)) {
            throw new RuntimeException("Unauthorized");
        }

        if (trade.getStatus() != P2PTrade.P2PTradeStatus.PENDING_PAYMENT) {
            throw new RuntimeException("Trade is not in pending payment status");
        }

        trade.setStatus(P2PTrade.P2PTradeStatus.PAID);
        trade.setPaidAt(LocalDateTime.now());
        tradeRepository.save(trade);

        log.info("Trade {} marked as paid by buyer {}", tradeId, buyerId);
    }

    // 판매자: 에스크로 해제 (자산 전송)
    @Transactional
    public void releaseEscrow(Long tradeId, Long sellerId) {
        P2PTrade trade = tradeRepository.findById(tradeId)
                .orElseThrow(() -> new RuntimeException("Trade not found"));

        if (!trade.getSeller().getId().equals(sellerId)) {
            throw new RuntimeException("Unauthorized");
        }

        if (trade.getStatus() != P2PTrade.P2PTradeStatus.PAID) {
            throw new RuntimeException("Payment not confirmed yet");
        }

        // 판매자 지갑에서 차감 및 언락
        Wallet sellerWallet = walletRepository.findByUserIdAndAssetForUpdate(
                sellerId, trade.getAdvertisement().getAsset())
                .orElseThrow(() -> new RuntimeException("Seller wallet not found"));

        sellerWallet.unlock(trade.getEscrowAmount());
        sellerWallet.debit(trade.getEscrowAmount());

        // 구매자 지갑에 입금
        Wallet buyerWallet = walletRepository.findByUserIdAndAssetForUpdate(
                trade.getBuyer().getId(), trade.getAdvertisement().getAsset())
                .orElseThrow(() -> new RuntimeException("Buyer wallet not found"));

        buyerWallet.credit(trade.getEscrowAmount());

        walletRepository.save(sellerWallet);
        walletRepository.save(buyerWallet);

        // 거래 완료
        trade.setStatus(P2PTrade.P2PTradeStatus.COMPLETED);
        trade.setReleasedAt(LocalDateTime.now());
        tradeRepository.save(trade);

        log.info("Escrow released for trade {}: {} {} transferred from seller {} to buyer {}",
                tradeId, trade.getEscrowAmount(), trade.getAdvertisement().getAsset(),
                sellerId, trade.getBuyer().getId());
    }

    // 거래 취소
    @Transactional
    public void cancelTrade(Long tradeId, Long userId) {
        P2PTrade trade = tradeRepository.findById(tradeId)
                .orElseThrow(() -> new RuntimeException("Trade not found"));

        // 권한 체크
        if (!trade.getBuyer().getId().equals(userId) && !trade.getSeller().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        // 결제 전에만 취소 가능
        if (trade.getStatus() != P2PTrade.P2PTradeStatus.PENDING_PAYMENT) {
            throw new RuntimeException("Can only cancel before payment");
        }

        // 에스크로 환원
        refundEscrow(trade);

        // 광고 가능량 복구
        P2PAdvertisement ad = trade.getAdvertisement();
        ad.setAvailableAmount(ad.getAvailableAmount().add(trade.getAmount()));
        advertisementRepository.save(ad);

        trade.setStatus(P2PTrade.P2PTradeStatus.CANCELLED);
        tradeRepository.save(trade);

        log.info("Trade {} cancelled by user {}", tradeId, userId);
    }

    // 분쟁 제기
    @Transactional
    public void openDispute(Long tradeId, Long userId, String reason) {
        P2PTrade trade = tradeRepository.findById(tradeId)
                .orElseThrow(() -> new RuntimeException("Trade not found"));

        if (!trade.getBuyer().getId().equals(userId) && !trade.getSeller().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        trade.setStatus(P2PTrade.P2PTradeStatus.DISPUTED);
        trade.setDisputeReason(reason);
        tradeRepository.save(trade);

        log.warn("Dispute opened for trade {} by user {}: {}", tradeId, userId, reason);
    }

    // 만료된 거래 자동 취소 (스케줄러)
    @Scheduled(fixedDelay = 60000) // 1분마다
    @Transactional
    public void cancelExpiredTrades() {
        List<P2PTrade> expiredTrades = tradeRepository.findExpiredTrades(
                P2PTrade.P2PTradeStatus.PENDING_PAYMENT, LocalDateTime.now());

        for (P2PTrade trade : expiredTrades) {
            try {
                refundEscrow(trade);

                // 광고 가능량 복구
                P2PAdvertisement ad = trade.getAdvertisement();
                ad.setAvailableAmount(ad.getAvailableAmount().add(trade.getAmount()));
                advertisementRepository.save(ad);

                trade.setStatus(P2PTrade.P2PTradeStatus.CANCELLED);
                tradeRepository.save(trade);

                log.info("Expired trade {} auto-cancelled", trade.getId());
            } catch (Exception e) {
                log.error("Error cancelling expired trade {}: {}", trade.getId(), e.getMessage());
            }
        }
    }

    private void refundEscrow(P2PTrade trade) {
        // 에스크로된 자산 환원
        Wallet sellerWallet = walletRepository.findByUserIdAndAssetForUpdate(
                trade.getSeller().getId(), trade.getAdvertisement().getAsset())
                .orElseThrow(() -> new RuntimeException("Seller wallet not found"));

        sellerWallet.unlock(trade.getEscrowAmount());
        walletRepository.save(sellerWallet);
    }

    @Transactional(readOnly = true)
    public List<P2PAdvertisement> getActiveAdvertisements(String asset, String fiatCurrency,
                                                           P2PAdvertisement.TradeType type) {
        return advertisementRepository.findActiveByAssetAndCurrencyAndType(asset, fiatCurrency, type);
    }

    @Transactional(readOnly = true)
    public List<P2PTrade> getUserTrades(Long userId) {
        return tradeRepository.findByBuyerIdOrSellerIdOrderByCreatedAtDesc(userId, userId);
    }
}
