package de.rentacar.booking.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository Interface für Booking Aggregate (Domain Layer)
 */
public interface BookingRepository {
    Booking save(Booking booking);
    Optional<Booking> findById(Long id);
    List<Booking> findAll();
    List<Booking> findByCustomerId(Long customerId);
    List<Booking> findByVehicleId(Long vehicleId);
    List<Booking> findOverlappingBookings(Long vehicleId, LocalDate startDate, LocalDate endDate);
    List<Booking> findActiveBookingsByVehicle(Long vehicleId);
}

