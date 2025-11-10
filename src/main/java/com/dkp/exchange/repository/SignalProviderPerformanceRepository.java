package com.dkp.exchange.repository;

import com.dkp.exchange.model.SignalProviderPerformance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SignalProviderPerformanceRepository extends JpaRepository<SignalProviderPerformance, Long> {
    List<SignalProviderPerformance> findByProviderId(Long providerId);

    Optional<SignalProviderPerformance> findByProviderIdAndDate(Long providerId, LocalDate date);

    @Query("SELECT p FROM SignalProviderPerformance p WHERE p.provider.id = :providerId AND p.date >= :startDate AND p.date <= :endDate ORDER BY p.date ASC")
    List<SignalProviderPerformance> findByProviderIdBetweenDates(
            @Param("providerId") Long providerId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT p FROM SignalProviderPerformance p WHERE p.provider.id = :providerId ORDER BY p.date DESC")
    List<SignalProviderPerformance> findByProviderIdOrderByDateDesc(@Param("providerId") Long providerId);
}
