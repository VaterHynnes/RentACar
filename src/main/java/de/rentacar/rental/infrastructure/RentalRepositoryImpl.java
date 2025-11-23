package de.rentacar.rental.infrastructure;

import de.rentacar.rental.domain.Rental;
import de.rentacar.rental.domain.RentalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository Implementation (Infrastructure Layer)
 */
@Repository
@RequiredArgsConstructor
public class RentalRepositoryImpl implements RentalRepository {

    private final RentalJpaRepository jpaRepository;

    @Override
    public Rental save(Rental rental) {
        return jpaRepository.save(rental);
    }

    @Override
    public Optional<Rental> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<Rental> findByBookingId(Long bookingId) {
        return jpaRepository.findByBookingId(bookingId);
    }

    @Override
    public List<Rental> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public List<Rental> findByCustomerId(Long customerId) {
        return jpaRepository.findByCustomerId(customerId);
    }

    @Override
    public List<Rental> findByVehicleId(Long vehicleId) {
        return jpaRepository.findByVehicleId(vehicleId);
    }
}

