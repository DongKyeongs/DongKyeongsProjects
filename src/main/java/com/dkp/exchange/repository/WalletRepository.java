package com.dkp.exchange.repository;

import com.dkp.exchange.model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {
    List<Wallet> findByUserId(Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM Wallet w WHERE w.user.id = :userId AND w.asset = :asset")
    Optional<Wallet> findByUserIdAndAssetForUpdate(@Param("userId") Long userId, @Param("asset") String asset);

    Optional<Wallet> findByUserIdAndAsset(Long userId, String asset);

    Optional<Wallet> findByAddress(String address);
}
