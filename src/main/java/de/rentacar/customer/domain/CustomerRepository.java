package de.rentacar.customer.domain;

import java.util.List;
import java.util.Optional;

/**
 * Repository Interface für Customer Aggregate (Domain Layer)
 */
public interface CustomerRepository {
    Customer save(Customer customer);
    Optional<Customer> findById(Long id);
    Optional<Customer> findByUsername(String username);
    List<Customer> findAll();
    void deleteById(Long id);
}

