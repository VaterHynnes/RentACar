package de.rentacar.customer.infrastructure;

import de.rentacar.customer.domain.Customer;
import de.rentacar.customer.domain.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository Implementation (Infrastructure Layer)
 */
@Repository
@RequiredArgsConstructor
public class CustomerRepositoryImpl implements CustomerRepository {

    private final CustomerJpaRepository jpaRepository;

    @Override
    public Customer save(Customer customer) {
        return jpaRepository.save(customer);
    }

    @Override
    public Optional<Customer> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<Customer> findByUsername(String username) {
        return jpaRepository.findByUsername(username);
    }

    @Override
    public List<Customer> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
}

