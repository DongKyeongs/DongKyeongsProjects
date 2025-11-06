package com.dkp.exchange.repository;

import com.dkp.exchange.model.Order;
import com.dkp.exchange.model.OrderStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Order> findBySymbolAndStatusOrderByCreatedAtDesc(String symbol, OrderStatus status);

    List<Order> findBySymbolAndStatus(String symbol, OrderStatus status);

    @Query("SELECT o FROM Order o WHERE o.user.id = :userId AND o.symbol = :symbol ORDER BY o.createdAt DESC")
    List<Order> findByUserIdAndSymbol(@Param("userId") Long userId, @Param("symbol") String symbol);

    @Query("SELECT o FROM Order o WHERE o.user.id = :userId AND o.status = :status ORDER BY o.createdAt DESC")
    List<Order> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") OrderStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM Order o WHERE o.id = :id")
    Optional<Order> findByIdWithLock(@Param("id") Long id);

    // Admin management methods
    long countByStatus(OrderStatus status);
}
