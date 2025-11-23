package de.rentacar.vehicle.domain;

import de.rentacar.shared.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Aggregate Root für Fahrzeuge (Vehicle Context)
 */
@Entity
@Table(name = "vehicles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vehicle extends BaseEntity {

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "license_plate"))
    private LicensePlate licensePlate;

    @Column(nullable = false, length = 50)
    private String brand;

    @Column(nullable = false, length = 50)
    private String model;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VehicleType type;

    @Column(nullable = false)
    private Long mileage;

    @Column(nullable = false, length = 100)
    private String location; // Filiale/Standort

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private VehicleStatus status = VehicleStatus.VERFÜGBAR;

    @Column(nullable = false)
    private Double dailyPrice;

    /**
     * Domain-Methode: Fahrzeug als vermietet markieren
     */
    public void markAsRented() {
        if (this.status != VehicleStatus.VERFÜGBAR) {
            throw new IllegalStateException("Fahrzeug ist nicht verfügbar für Vermietung");
        }
        this.status = VehicleStatus.VERMIETET;
    }

    /**
     * Domain-Methode: Fahrzeug als verfügbar markieren
     */
    public void markAsAvailable() {
        this.status = VehicleStatus.VERFÜGBAR;
    }

    /**
     * Domain-Methode: Fahrzeug in Wartung setzen
     */
    public void markAsMaintenance() {
        this.status = VehicleStatus.WARTUNG;
    }

    /**
     * Domain-Methode: Fahrzeug außer Betrieb setzen
     */
    public void markAsOutOfService() {
        this.status = VehicleStatus.AUSSER_BETRIEB;
    }

    /**
     * Domain-Methode: Kilometerstand aktualisieren
     */
    public void updateMileage(Long newMileage) {
        if (newMileage == null || newMileage < this.mileage) {
            throw new IllegalArgumentException("Neuer Kilometerstand muss größer als der aktuelle sein");
        }
        this.mileage = newMileage;
    }

    /**
     * Domain-Methode: Prüft ob Fahrzeug verfügbar ist
     */
    public boolean isAvailable() {
        return this.status == VehicleStatus.VERFÜGBAR;
    }
}

