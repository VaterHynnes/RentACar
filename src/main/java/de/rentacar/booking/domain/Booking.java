package de.rentacar.booking.domain;

import de.rentacar.shared.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Aggregate Root für Buchungen (Booking Context)
 */
@Entity
@Table(name = "bookings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking extends BaseEntity {

    @Column(nullable = false)
    private Long customerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private de.rentacar.vehicle.domain.Vehicle vehicle;

    @Column(nullable = false)
    private LocalDate pickupDate;

    @Column(nullable = false)
    private LocalDate returnDate;

    @Column(nullable = false, length = 100)
    private String pickupLocation;

    @Column(nullable = false, length = 100)
    private String returnLocation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private BookingStatus status = BookingStatus.ANFRAGE;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    private LocalDateTime cancellationDate;

    /**
     * Domain-Methode: Buchung bestätigen
     */
    public void confirm() {
        if (this.status != BookingStatus.ANFRAGE) {
            throw new IllegalStateException("Nur Anfragen können bestätigt werden");
        }
        this.status = BookingStatus.BESTÄTIGT;
    }

    /**
     * Domain-Methode: Buchung stornieren (bis 24h vor Abholung)
     */
    public void cancel() {
        if (this.status == BookingStatus.STORNIERT || this.status == BookingStatus.ABGESCHLOSSEN) {
            throw new IllegalStateException("Buchung kann nicht mehr storniert werden");
        }
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime pickupDateTime = pickupDate.atStartOfDay();
        LocalDateTime cancellationDeadline = pickupDateTime.minusHours(24);
        
        if (now.isAfter(cancellationDeadline)) {
            throw new IllegalStateException("Stornierung nur bis 24 Stunden vor Abholung möglich");
        }
        
        this.status = BookingStatus.STORNIERT;
        this.cancellationDate = now;
    }

    /**
     * Domain-Methode: Buchung abschließen
     */
    public void complete() {
        if (this.status != BookingStatus.BESTÄTIGT) {
            throw new IllegalStateException("Nur bestätigte Buchungen können abgeschlossen werden");
        }
        this.status = BookingStatus.ABGESCHLOSSEN;
    }

    /**
     * Domain-Methode: Prüft ob Buchung aktiv ist (nicht storniert oder abgeschlossen)
     */
    public boolean isActive() {
        return this.status == BookingStatus.ANFRAGE || this.status == BookingStatus.BESTÄTIGT;
    }

    /**
     * Domain-Methode: Prüft ob Buchung überlappt mit gegebenem Zeitraum
     */
    public boolean overlapsWith(LocalDate startDate, LocalDate endDate) {
        return this.status == BookingStatus.BESTÄTIGT &&
               this.pickupDate.isBefore(endDate.plusDays(1)) &&
               this.returnDate.isAfter(startDate.minusDays(1));
    }
}

