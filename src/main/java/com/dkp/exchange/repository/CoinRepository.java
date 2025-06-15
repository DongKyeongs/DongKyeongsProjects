package com.dkp.exchange.repository;

import com.dkp.exchange.model.Coin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CoinRepository extends JpaRepository<Coin, Long> {
    Coin findBySymbol(String symbol);
} 