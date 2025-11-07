package com.nrjsingh1.system_design_experiment.repository;

import com.nrjsingh1.system_design_experiment.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    List<Product> findByCategory(String category);
    
    List<Product> findByStockLessThan(Integer minStock);
    
    @Query("SELECT p FROM Product p WHERE p.stock > 0 ORDER BY p.stock ASC")
    List<Product> findAvailableProductsOrderByStockAsc();
    
    boolean existsByName(String name);
}