package de.rentacar.booking.infrastructure;

import de.rentacar.booking.domain.Booking;
import de.rentacar.booking.domain.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository Implementation (Infrastructure Layer)
 */
@Repository
@RequiredArgsConstructor
public class BookingRepositoryImpl implements BookingRepository {

    private final BookingJpaRepository jpaRepository;

    @Override
    public Booking save(Booking booking) {
        return jpaRepository.save(booking);
    }

    @Override
    public Optional<Booking> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<Booking> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public List<Booking> findByCustomerId(Long customerId) {
        return jpaRepository.findByCustomerId(customerId);
    }

    @Override
    public List<Booking> findByVehicleId(Long vehicleId) {
        return jpaRepository.findByVehicleId(vehicleId);
    }

    @Override
    public List<Booking> findOverlappingBookings(Long vehicleId, LocalDate startDate, LocalDate endDate) {
        return jpaRepository.findOverlappingBookings(vehicleId, startDate, endDate);
    }

    @Override
    public List<Booking> findActiveBookingsByVehicle(Long vehicleId) {
        return jpaRepository.findActiveBookingsByVehicle(vehicleId);
    }
}

