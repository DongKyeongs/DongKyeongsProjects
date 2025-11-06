package com.dkp.exchange.repository;

import com.dkp.exchange.model.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
    List<Portfolio> findByUserId(Long userId);

    @Query("SELECT p FROM Portfolio p WHERE p.user.id = :userId AND p.asset = :asset")
    Optional<Portfolio> findByUserIdAndAsset(@Param("userId") Long userId, @Param("asset") String asset);

    @Query("SELECT p FROM Portfolio p WHERE p.user.id = :userId AND p.balance > 0")
    List<Portfolio> findActivePortfoliosByUserId(@Param("userId") Long userId);
}
