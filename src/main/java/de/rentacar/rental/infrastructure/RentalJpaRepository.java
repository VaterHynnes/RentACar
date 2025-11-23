package de.rentacar.rental.infrastructure;

import de.rentacar.rental.domain.Rental;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA Repository Implementation (Infrastructure Layer)
 */
@Repository
public interface RentalJpaRepository extends JpaRepository<Rental, Long> {
    Optional<Rental> findByBookingId(Long bookingId);
    List<Rental> findByCustomerId(Long customerId);
    List<Rental> findByVehicleId(Long vehicleId);
}

