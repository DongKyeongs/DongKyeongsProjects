package com.dkp.exchange.repository;

import com.dkp.exchange.model.ExchangeSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExchangeSettingsRepository extends JpaRepository<ExchangeSettings, Long> {
    @Query("SELECT e FROM ExchangeSettings e WHERE e.user.id = :userId")
    Optional<ExchangeSettings> findByUserId(@Param("userId") Long userId);
}
