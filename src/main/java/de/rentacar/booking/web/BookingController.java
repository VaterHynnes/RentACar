package de.rentacar.booking.web;

import de.rentacar.booking.application.BookingService;
import de.rentacar.booking.domain.Booking;
import de.rentacar.vehicle.domain.Vehicle;
import de.rentacar.vehicle.domain.VehicleType;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.List;

/**
 * REST Controller für Buchungsverwaltung
 */
@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @GetMapping("/search")
    public ResponseEntity<List<Vehicle>> searchAvailableVehicles(
            @RequestParam VehicleType vehicleType,
            @RequestParam String location,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<Vehicle> vehicles = bookingService.searchAvailableVehicles(
                vehicleType, location, startDate, endDate);
        return ResponseEntity.ok(vehicles);
    }

    @PostMapping
    public ResponseEntity<Booking> createBooking(@RequestBody CreateBookingRequest request,
                                                Authentication authentication,
                                                HttpServletRequest httpRequest) {
        Booking booking = bookingService.createBooking(
                request.customerId(),
                request.vehicleId(),
                request.pickupDate(),
                request.returnDate(),
                request.pickupLocation(),
                request.returnLocation(),
                authentication.getName(),
                httpRequest.getRemoteAddr()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(booking);
    }

    @PutMapping("/{id}/confirm")
    public ResponseEntity<Void> confirmBooking(@PathVariable Long id,
                                              Authentication authentication,
                                              HttpServletRequest httpRequest) {
        bookingService.confirmBooking(id, authentication.getName(), httpRequest.getRemoteAddr());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelBooking(@PathVariable Long id,
                                             Authentication authentication,
                                             HttpServletRequest httpRequest) {
        bookingService.cancelBooking(id, authentication.getName(), httpRequest.getRemoteAddr());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<Booking>> getBookingHistory(@PathVariable Long customerId) {
        return ResponseEntity.ok(bookingService.getBookingHistory(customerId));
    }

    public record CreateBookingRequest(
            Long customerId,
            Long vehicleId,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate pickupDate,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate returnDate,
            String pickupLocation,
            String returnLocation
    ) {}
}

