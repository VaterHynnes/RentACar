package de.rentacar.booking.application;

import de.rentacar.booking.domain.*;
import de.rentacar.customer.domain.CustomerRepository;
import de.rentacar.shared.domain.AuditService;
import de.rentacar.vehicle.domain.Vehicle;
import de.rentacar.vehicle.domain.VehicleRepository;
import de.rentacar.vehicle.domain.VehicleType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Application Service für Buchungsverwaltung (Use Cases)
 */
@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final VehicleRepository vehicleRepository;
    private final CustomerRepository customerRepository;
    private final PriceCalculationService priceCalculationService;
    private final AvailabilityService availabilityService;
    private final AuditService auditService;

    /**
     * Use Case: Fahrzeuge suchen (Zeitraum, Typ, Standort)
     */
    @Transactional(readOnly = true)
    public List<Vehicle> searchAvailableVehicles(VehicleType vehicleType, String location, 
                                                  LocalDate startDate, LocalDate endDate) {
        validateDateRange(startDate, endDate);
        return vehicleRepository.findAvailableVehicles(vehicleType, location, startDate, endDate);
    }

    /**
     * Use Case: Buchung erstellen mit Verfügbarkeitsprüfung
     */
    @Transactional
    public Booking createBooking(Long customerId, Long vehicleId, LocalDate pickupDate, 
                                 LocalDate returnDate, String pickupLocation, String returnLocation,
                                 String username, String ipAddress) {
        // Validierung
        validateDateRange(pickupDate, returnDate);
        
        // Kunde prüfen
        customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Kunde nicht gefunden"));

        // Fahrzeug prüfen
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Fahrzeug nicht gefunden"));

        // Verfügbarkeitsprüfung (verhindert Überbuchung)
        if (!availabilityService.isVehicleAvailable(vehicleId, pickupDate, returnDate)) {
            throw new IllegalStateException("Fahrzeug ist im angegebenen Zeitraum nicht verfügbar");
        }

        // Preis berechnen
        BigDecimal totalPrice = priceCalculationService.calculateTotalPrice(
                vehicle.getType(), pickupDate, returnDate);

        // Buchung erstellen
        Booking booking = Booking.builder()
                .customerId(customerId)
                .vehicle(vehicle)
                .pickupDate(pickupDate)
                .returnDate(returnDate)
                .pickupLocation(pickupLocation)
                .returnLocation(returnLocation)
                .totalPrice(totalPrice)
                .status(BookingStatus.ANFRAGE)
                .build();

        Booking savedBooking = bookingRepository.save(booking);

        // Audit-Log
        auditService.logAction(username, "BOOKING_CREATED", "Booking", 
                savedBooking.getId() != null ? savedBooking.getId().toString() : "NEW", 
                String.format("Buchung erstellt für Fahrzeug %s", vehicle.getLicensePlate()),
                ipAddress);

        return savedBooking;
    }

    /**
     * Use Case: Buchung bestätigen
     */
    @Transactional
    public void confirmBooking(Long bookingId, String username, String ipAddress) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Buchung nicht gefunden"));

        // Verfügbarkeit nochmal prüfen
        if (!availabilityService.isVehicleAvailable(booking.getVehicle().getId(), 
                booking.getPickupDate(), booking.getReturnDate())) {
            throw new IllegalStateException("Fahrzeug ist nicht mehr verfügbar");
        }

        booking.confirm();
        booking.getVehicle().markAsRented();
        
        bookingRepository.save(booking);
        vehicleRepository.save(booking.getVehicle());

        auditService.logAction(username, "BOOKING_CONFIRMED", "Booking", 
                bookingId.toString(), "Buchung bestätigt", ipAddress);
    }

    /**
     * Use Case: Buchung stornieren (bis 24h vor Abholung)
     */
    @Transactional
    public void cancelBooking(Long bookingId, String username, String ipAddress) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Buchung nicht gefunden"));

        booking.cancel();
        
        // Fahrzeug wieder verfügbar machen, wenn es bestätigt war
        if (booking.getStatus() == BookingStatus.STORNIERT) {
            booking.getVehicle().markAsAvailable();
            vehicleRepository.save(booking.getVehicle());
        }
        
        bookingRepository.save(booking);

        auditService.logAction(username, "BOOKING_CANCELLED", "Booking", 
                bookingId.toString(), "Buchung storniert", ipAddress);
    }

    /**
     * Use Case: Buchungshistorie pro Kunde
     */
    @Transactional(readOnly = true)
    public List<Booking> getBookingHistory(Long customerId) {
        return bookingRepository.findByCustomerId(customerId);
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Datum darf nicht null sein");
        }
        if (startDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Abholdatum darf nicht in der Vergangenheit liegen");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Abholdatum muss vor Rückgabedatum liegen");
        }
    }
}

