package com.dkp.exchange.repository;

import com.dkp.exchange.model.TwoFactorAuth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TwoFactorAuthRepository extends JpaRepository<TwoFactorAuth, Long> {
    @Query("SELECT t FROM TwoFactorAuth t WHERE t.user.id = :userId")
    Optional<TwoFactorAuth> findByUserId(@Param("userId") Long userId);
}
