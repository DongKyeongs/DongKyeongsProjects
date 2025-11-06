package com.dkp.exchange.repository;

import com.dkp.exchange.model.P2PTrade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface P2PTradeRepository extends JpaRepository<P2PTrade, Long> {
    List<P2PTrade> findByBuyerIdOrSellerIdOrderByCreatedAtDesc(Long buyerId, Long sellerId);

    @Query("SELECT t FROM P2PTrade t WHERE t.status = :status AND t.paymentDeadline < :now")
    List<P2PTrade> findExpiredTrades(@Param("status") P2PTrade.P2PTradeStatus status, @Param("now") LocalDateTime now);

    List<P2PTrade> findByStatus(P2PTrade.P2PTradeStatus status);
}
