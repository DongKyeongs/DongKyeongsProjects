package com.dkp.exchange.repository;

import com.dkp.exchange.model.Fee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FeeRepository extends JpaRepository<Fee, Long> {
    Optional<Fee> findBySymbolAndTypeAndActive(String symbol, Fee.FeeType type, boolean active);
    Optional<Fee> findBySymbolAndType(String symbol, Fee.FeeType type);
}
