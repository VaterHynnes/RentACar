package de.rentacar.rental.application;

import de.rentacar.booking.domain.Booking;
import de.rentacar.booking.domain.BookingRepository;
import de.rentacar.rental.domain.DamageReport;
import de.rentacar.rental.domain.Rental;
import de.rentacar.rental.domain.RentalRepository;
import de.rentacar.shared.domain.AuditService;
import de.rentacar.vehicle.domain.Vehicle;
import de.rentacar.vehicle.domain.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Application Service für Vermietungsprozess (Use Cases)
 */
@Service
@RequiredArgsConstructor
public class RentalService {

    private final RentalRepository rentalRepository;
    private final BookingRepository bookingRepository;
    private final VehicleRepository vehicleRepository;
    private final AuditService auditService;

    /**
     * Use Case: Check-out durchführen (Übergabe)
     */
    @Transactional
    public Rental performCheckout(Long bookingId, Long mileage, String condition, 
                                  String username, String ipAddress) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Buchung nicht gefunden"));

        if (booking.getStatus() != de.rentacar.booking.domain.BookingStatus.BESTÄTIGT) {
            throw new IllegalStateException("Nur bestätigte Buchungen können ausgegeben werden");
        }

        Vehicle vehicle = booking.getVehicle();
        vehicle.updateMileage(mileage);

        Rental rental = Rental.builder()
                .bookingId(bookingId)
                .vehicleId(vehicle.getId())
                .customerId(booking.getCustomerId())
                .plannedPickupDate(booking.getPickupDate())
                .plannedReturnDate(booking.getReturnDate())
                .pickupMileage(mileage)
                .build();

        rental.performCheckout(mileage, condition);
        
        vehicle.markAsRented();
        
        Rental savedRental = rentalRepository.save(rental);
        vehicleRepository.save(vehicle);

        auditService.logAction(username, "RENTAL_CHECKOUT", "Rental", 
                savedRental.getId().toString(), 
                String.format("Check-out für Fahrzeug %s", vehicle.getLicensePlate()),
                ipAddress);

        return savedRental;
    }

    /**
     * Use Case: Check-in durchführen (Rückgabe)
     */
    @Transactional
    public Rental performCheckin(Long rentalId, Long mileage, String condition, 
                                 String username, String ipAddress) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new IllegalArgumentException("Vermietung nicht gefunden"));

        rental.performCheckin(mileage, condition);

        Vehicle vehicle = vehicleRepository.findById(rental.getVehicleId())
                .orElseThrow(() -> new IllegalArgumentException("Fahrzeug nicht gefunden"));
        
        vehicle.updateMileage(mileage);
        vehicle.markAsAvailable();

        // Prüfe auf Verspätung
        if (rental.getActualReturnTime().toLocalDate().isAfter(rental.getPlannedReturnDate())) {
            long daysLate = java.time.temporal.ChronoUnit.DAYS.between(
                    rental.getPlannedReturnDate(), 
                    rental.getActualReturnTime().toLocalDate());
            BigDecimal lateFee = BigDecimal.valueOf(50.00).multiply(BigDecimal.valueOf(daysLate));
            rental.addLateReturnFee(lateFee, 
                    String.format("Verspätung um %d Tag(e)", daysLate));
        }

        Rental savedRental = rentalRepository.save(rental);
        vehicleRepository.save(vehicle);

        // Buchung abschließen
        Booking booking = bookingRepository.findById(rental.getBookingId())
                .orElseThrow(() -> new IllegalArgumentException("Buchung nicht gefunden"));
        booking.complete();
        bookingRepository.save(booking);

        auditService.logAction(username, "RENTAL_CHECKIN", "Rental", 
                rentalId.toString(), 
                String.format("Check-in für Fahrzeug %s", vehicle.getLicensePlate()),
                ipAddress);

        return savedRental;
    }

    /**
     * Use Case: Schadensbericht erstellen
     */
    @Transactional
    public DamageReport createDamageReport(Long rentalId, String description, 
                                          BigDecimal repairCost, String notes,
                                          String username, String ipAddress) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new IllegalArgumentException("Vermietung nicht gefunden"));

        DamageReport damageReport = DamageReport.builder()
                .rentalId(rentalId)
                .description(description)
                .repairCost(repairCost)
                .notes(notes)
                .build();

        rental.registerDamage(repairCost, description);
        
        // TODO: DamageReport Repository hinzufügen wenn benötigt
        rentalRepository.save(rental);

        auditService.logAction(username, "DAMAGE_REPORT_CREATED", "DamageReport", 
                rentalId.toString(), 
                String.format("Schadensbericht erstellt: %s", description),
                ipAddress);

        return damageReport;
    }
}

