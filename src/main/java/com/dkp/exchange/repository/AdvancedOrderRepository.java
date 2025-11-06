package com.dkp.exchange.repository;

import com.dkp.exchange.model.AdvancedOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdvancedOrderRepository extends JpaRepository<AdvancedOrder, Long> {
    List<AdvancedOrder> findByUserIdAndStatus(Long userId, AdvancedOrder.AdvancedOrderStatus status);

    @Query("SELECT a FROM AdvancedOrder a WHERE a.status = 'PENDING' AND a.type = :type")
    List<AdvancedOrder> findPendingByType(@Param("type") AdvancedOrder.AdvancedOrderType type);

    @Query("SELECT a FROM AdvancedOrder a WHERE a.status = 'PENDING' AND a.symbol = :symbol")
    List<AdvancedOrder> findPendingBySymbol(@Param("symbol") String symbol);
}
