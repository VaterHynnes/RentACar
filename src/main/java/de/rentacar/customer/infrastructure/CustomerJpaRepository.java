package de.rentacar.customer.infrastructure;

import de.rentacar.customer.domain.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * JPA Repository Implementation (Infrastructure Layer)
 */
@Repository
public interface CustomerJpaRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByUsername(String username);
}

