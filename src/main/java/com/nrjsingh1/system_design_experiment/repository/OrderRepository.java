package com.nrjsingh1.system_design_experiment.repository;

import com.nrjsingh1.system_design_experiment.model.Order;
import com.nrjsingh1.system_design_experiment.model.Order.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderItems WHERE o.id = ?1")
    Order findByIdWithItems(Long id);
    
    List<Order> findByCustomerId(Long customerId);
    
    List<Order> findByStatus(OrderStatus status);
    
    Page<Order> findByOrderDateBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    
    @Query("SELECT o FROM Order o WHERE o.status = ?1 AND o.orderDate < ?2")
    List<Order> findStaleOrders(OrderStatus status, LocalDateTime before);
    
    // Find orders needing attention (PENDING or PROCESSING status for more than 24 hours)
    @Query("SELECT o FROM Order o WHERE o.status IN ('PENDING', 'PROCESSING') AND o.orderDate < ?1")
    List<Order> findOrdersNeedingAttention(LocalDateTime cutoffDate);
}