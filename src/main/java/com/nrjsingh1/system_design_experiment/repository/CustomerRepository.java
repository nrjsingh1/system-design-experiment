package com.nrjsingh1.system_design_experiment.repository;

import com.nrjsingh1.system_design_experiment.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    
    Optional<Customer> findByEmail(String email);
    
    @Query("SELECT c FROM Customer c LEFT JOIN FETCH c.orders WHERE c.id = ?1")
    Optional<Customer> findByIdWithOrders(Long id);
    
    List<Customer> findByLastNameOrderByFirstNameAsc(String lastName);
    
    boolean existsByEmail(String email);
}