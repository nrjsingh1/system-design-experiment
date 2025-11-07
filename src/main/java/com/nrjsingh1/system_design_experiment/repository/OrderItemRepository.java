package com.nrjsingh1.system_design_experiment.repository;

import com.nrjsingh1.system_design_experiment.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    
    List<OrderItem> findByOrderId(Long orderId);
    
    List<OrderItem> findByProductId(Long productId);
    
    // Find top selling products within a date range
    @Query("""
        SELECT oi.product.id, oi.product.name, SUM(oi.quantity) as totalQuantity 
        FROM OrderItem oi 
        WHERE oi.order.orderDate BETWEEN ?1 AND ?2 
        GROUP BY oi.product.id, oi.product.name 
        ORDER BY totalQuantity DESC
        """)
    List<Object[]> findTopSellingProducts(LocalDateTime startDate, LocalDateTime endDate);
    
    // Calculate revenue by product
    @Query("""
        SELECT oi.product.id, oi.product.name, SUM(oi.quantity * oi.price) as revenue 
        FROM OrderItem oi 
        WHERE oi.order.orderDate BETWEEN ?1 AND ?2 
        GROUP BY oi.product.id, oi.product.name 
        ORDER BY revenue DESC
        """)
    List<Object[]> calculateRevenueByProduct(LocalDateTime startDate, LocalDateTime endDate);
}