package de.rentacar.booking.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Domain Service für Verfügbarkeitsprüfung (verhindert Überbuchung)
 */
@Service
@RequiredArgsConstructor
public class AvailabilityService {

    private final BookingRepository bookingRepository;

    /**
     * Prüft ob ein Fahrzeug im angegebenen Zeitraum verfügbar ist
     * WICHTIG: Verhindert Überbuchungen durch Prüfung auf überlappende Buchungen
     */
    public boolean isVehicleAvailable(Long vehicleId, LocalDate startDate, LocalDate endDate) {
        // Suche nach überlappenden bestätigten Buchungen
        List<Booking> overlappingBookings = bookingRepository.findOverlappingBookings(
                vehicleId, startDate, endDate);
        
        // Wenn überlappende Buchungen existieren, ist das Fahrzeug nicht verfügbar
        return overlappingBookings.isEmpty();
    }
}

