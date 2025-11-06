package com.dkp.exchange.repository;

import com.dkp.exchange.model.Trade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TradeRepository extends JpaRepository<Trade, Long> {
    List<Trade> findBySymbolOrderByExecutedAtDesc(String symbol);

    @Query("SELECT t FROM Trade t WHERE t.symbol = :symbol AND t.executedAt >= :startTime ORDER BY t.executedAt DESC")
    List<Trade> findBySymbolAndExecutedAtAfter(@Param("symbol") String symbol, @Param("startTime") LocalDateTime startTime);

    @Query("SELECT t FROM Trade t WHERE t.buyOrder.user.id = :userId OR t.sellOrder.user.id = :userId ORDER BY t.executedAt DESC")
    List<Trade> findByUserId(@Param("userId") Long userId);

    @Query("SELECT t FROM Trade t WHERE t.symbol = :symbol AND t.executedAt BETWEEN :startTime AND :endTime")
    List<Trade> findBySymbolAndTimePeriod(@Param("symbol") String symbol, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
}
