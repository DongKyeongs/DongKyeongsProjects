package com.dkp.exchange.repository;

import com.dkp.exchange.model.ChartData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChartDataRepository extends JpaRepository<ChartData, Long> {
    @Query("SELECT c FROM ChartData c WHERE c.symbol = :symbol AND c.timeFrame = :timeFrame AND c.time >= :startTime ORDER BY c.time ASC")
    List<ChartData> findBySymbolAndTimeFrameAndTimeAfter(@Param("symbol") String symbol, @Param("timeFrame") String timeFrame, @Param("startTime") LocalDateTime startTime);

    @Query("SELECT c FROM ChartData c WHERE c.symbol = :symbol AND c.timeFrame = :timeFrame AND c.time BETWEEN :startTime AND :endTime ORDER BY c.time ASC")
    List<ChartData> findBySymbolAndTimeFrameAndTimeBetween(@Param("symbol") String symbol, @Param("timeFrame") String timeFrame, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    @Query("SELECT c FROM ChartData c WHERE c.symbol = :symbol AND c.timeFrame = :timeFrame ORDER BY c.time DESC")
    List<ChartData> findBySymbolAndTimeFrameOrderByTimeDesc(@Param("symbol") String symbol, @Param("timeFrame") String timeFrame);
}
