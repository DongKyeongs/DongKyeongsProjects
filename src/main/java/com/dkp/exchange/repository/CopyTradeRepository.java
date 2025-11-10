package com.dkp.exchange.repository;

import com.dkp.exchange.model.CopyTrade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CopyTradeRepository extends JpaRepository<CopyTrade, Long> {
    List<CopyTrade> findByFollowerId(Long followerId);

    List<CopyTrade> findByProviderId(Long providerId);

    @Query("SELECT c FROM CopyTrade c WHERE c.follower.id = :followerId AND c.status = 'ACTIVE'")
    List<CopyTrade> findActiveByFollowerId(@Param("followerId") Long followerId);

    @Query("SELECT c FROM CopyTrade c WHERE c.provider.id = :providerId AND c.status = 'ACTIVE'")
    List<CopyTrade> findActiveByProviderId(@Param("providerId") Long providerId);

    @Query("SELECT COUNT(c) FROM CopyTrade c WHERE c.provider.id = :providerId AND c.status = 'ACTIVE'")
    long countActiveFollowers(@Param("providerId") Long providerId);
}
