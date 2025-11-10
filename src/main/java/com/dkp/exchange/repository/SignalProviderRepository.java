package com.dkp.exchange.repository;

import com.dkp.exchange.model.SignalProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SignalProviderRepository extends JpaRepository<SignalProvider, Long> {
    Optional<SignalProvider> findByUserId(Long userId);

    List<SignalProvider> findByStatus(SignalProvider.ProviderStatus status);

    @Query("SELECT p FROM SignalProvider p WHERE p.status = 'ACTIVE' AND p.acceptingFollowers = true ORDER BY p.ranking ASC")
    List<SignalProvider> findActiveProviders();

    @Query("SELECT p FROM SignalProvider p WHERE p.status = 'ACTIVE' ORDER BY p.totalReturnPercent DESC")
    List<SignalProvider> findTopPerformers();

    @Query("SELECT p FROM SignalProvider p WHERE p.status = 'ACTIVE' ORDER BY p.followerCount DESC")
    List<SignalProvider> findMostFollowed();

    @Query("SELECT p FROM SignalProvider p WHERE p.status = 'ACTIVE' AND p.nickname LIKE %:keyword%")
    List<SignalProvider> searchByNickname(@Param("keyword") String keyword);
}
