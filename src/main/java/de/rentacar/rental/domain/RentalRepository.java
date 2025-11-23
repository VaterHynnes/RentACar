package de.rentacar.rental.domain;

import java.util.List;
import java.util.Optional;

/**
 * Repository Interface für Rental Aggregate (Domain Layer)
 */
public interface RentalRepository {
    Rental save(Rental rental);
    Optional<Rental> findById(Long id);
    Optional<Rental> findByBookingId(Long bookingId);
    List<Rental> findAll();
    List<Rental> findByCustomerId(Long customerId);
    List<Rental> findByVehicleId(Long vehicleId);
}

