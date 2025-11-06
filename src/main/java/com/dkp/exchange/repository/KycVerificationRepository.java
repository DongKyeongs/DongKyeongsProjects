package com.dkp.exchange.repository;

import com.dkp.exchange.model.KycVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KycVerificationRepository extends JpaRepository<KycVerification, Long> {
    @Query("SELECT k FROM KycVerification k WHERE k.user.id = :userId")
    Optional<KycVerification> findByUserId(@Param("userId") Long userId);

    List<KycVerification> findByStatus(KycVerification.VerificationStatus status);

    @Query("SELECT k FROM KycVerification k WHERE k.status = 'PENDING' OR k.status = 'REVIEWING'")
    List<KycVerification> findPendingVerifications();

    @Query("SELECT COUNT(k) FROM KycVerification k WHERE k.status = 'PENDING' OR k.status = 'REVIEWING'")
    long countPendingVerifications();
}
