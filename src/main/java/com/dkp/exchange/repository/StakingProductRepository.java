package com.dkp.exchange.repository;

import com.dkp.exchange.model.StakingProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StakingProductRepository extends JpaRepository<StakingProduct, Long> {
    List<StakingProduct> findByActive(boolean active);
    List<StakingProduct> findByAssetAndActive(String asset, boolean active);
}
