package de.rentacar.rental.domain;

import de.rentacar.shared.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Aggregate Root für Vermietungen (Rental Context)
 */
@Entity
@Table(name = "rentals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rental extends BaseEntity {

    @Column(nullable = false)
    private Long bookingId;

    @Column(nullable = false)
    private Long vehicleId;

    @Column(nullable = false)
    private Long customerId;

    @Column(nullable = false)
    private LocalDate plannedPickupDate;

    @Column(nullable = false)
    private LocalDate plannedReturnDate;

    private LocalDateTime actualPickupTime;
    private LocalDateTime actualReturnTime;

    @Column(nullable = false)
    private Long pickupMileage;

    private Long returnMileage;

    @Column(length = 500)
    private String pickupCondition;

    @Column(length = 500)
    private String returnCondition;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private RentalStatus status = RentalStatus.AUSGEGEBEN;

    @Column(precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal additionalCosts = BigDecimal.ZERO;

    @Column(length = 1000)
    private String additionalCostsDescription;

    /**
     * Domain-Methode: Check-out durchführen (Übergabe)
     */
    public void performCheckout(Long mileage, String condition) {
        if (mileage == null || mileage < 0) {
            throw new IllegalArgumentException("Kilometerstand muss gültig sein");
        }
        if (condition == null || condition.trim().isEmpty()) {
            throw new IllegalArgumentException("Zustand muss angegeben werden");
        }
        
        this.pickupMileage = mileage;
        this.pickupCondition = condition;
        this.actualPickupTime = LocalDateTime.now();
        this.status = RentalStatus.AUSGEGEBEN;
    }

    /**
     * Domain-Methode: Check-in durchführen (Rückgabe)
     */
    public void performCheckin(Long mileage, String condition) {
        if (mileage == null || mileage < this.pickupMileage) {
            throw new IllegalArgumentException("Rückgabe-Kilometerstand muss größer als Abhol-Kilometerstand sein");
        }
        if (condition == null || condition.trim().isEmpty()) {
            throw new IllegalArgumentException("Zustand muss angegeben werden");
        }
        
        this.returnMileage = mileage;
        this.returnCondition = condition;
        this.actualReturnTime = LocalDateTime.now();
        this.status = RentalStatus.ZURÜCKGEKEHRT;
    }

    /**
     * Domain-Methode: Schaden registrieren
     */
    public void registerDamage(BigDecimal repairCost, String description) {
        if (repairCost == null || repairCost.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Reparaturkosten müssen gültig sein");
        }
        
        this.status = RentalStatus.MIT_SCHADEN;
        this.additionalCosts = this.additionalCosts.add(repairCost);
        if (this.additionalCostsDescription == null) {
            this.additionalCostsDescription = "";
        }
        this.additionalCostsDescription += "\nSchaden: " + description + " (Kosten: " + repairCost + " EUR)";
    }

    /**
     * Domain-Methode: Verspätungsgebühr hinzufügen
     */
    public void addLateReturnFee(BigDecimal fee, String description) {
        if (fee == null || fee.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Verspätungsgebühr muss gültig sein");
        }
        
        this.additionalCosts = this.additionalCosts.add(fee);
        if (this.additionalCostsDescription == null) {
            this.additionalCostsDescription = "";
        }
        this.additionalCostsDescription += "\nVerspätung: " + description + " (Gebühr: " + fee + " EUR)";
    }
}

