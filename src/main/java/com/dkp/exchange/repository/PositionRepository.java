package com.dkp.exchange.repository;

import com.dkp.exchange.model.Position;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PositionRepository extends JpaRepository<Position, Long> {
    @Query("SELECT p FROM Position p WHERE p.marginAccount.user.id = :userId AND p.status = 'OPEN'")
    List<Position> findOpenPositionsByUserId(@Param("userId") Long userId);

    @Query("SELECT p FROM Position p WHERE p.status = 'OPEN'")
    List<Position> findAllOpenPositions();

    @Query("SELECT p FROM Position p WHERE p.marginAccount.id = :marginAccountId AND p.symbol = :symbol AND p.status = 'OPEN'")
    List<Position> findOpenPositionsByAccountAndSymbol(@Param("marginAccountId") Long marginAccountId, @Param("symbol") String symbol);
}
