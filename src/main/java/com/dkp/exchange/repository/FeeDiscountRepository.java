package com.dkp.exchange.repository;

import com.dkp.exchange.model.FeeDiscount;
import com.dkp.exchange.model.VipLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FeeDiscountRepository extends JpaRepository<FeeDiscount, Long> {
    Optional<FeeDiscount> findByVipLevel(VipLevel vipLevel);
    Optional<FeeDiscount> findByVipLevelAndActiveTrue(VipLevel vipLevel);
}
