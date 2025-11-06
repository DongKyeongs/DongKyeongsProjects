package com.dkp.exchange.repository;

import com.dkp.exchange.model.MarginAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MarginAccountRepository extends JpaRepository<MarginAccount, Long> {
    @Query("SELECT m FROM MarginAccount m WHERE m.user.id = :userId")
    Optional<MarginAccount> findByUserId(@Param("userId") Long userId);

    @Query("SELECT m FROM MarginAccount m WHERE m.status = 'ACTIVE'")
    List<MarginAccount> findAllActive();
}
