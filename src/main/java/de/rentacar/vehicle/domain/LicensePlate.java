package de.rentacar.vehicle.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Value Object für Kennzeichen
 */
@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class LicensePlate {

    @Column(nullable = false, unique = true, length = 20)
    private String value;

    public static LicensePlate of(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Kennzeichen darf nicht leer sein");
        }
        return new LicensePlate(value.trim().toUpperCase());
    }

    @Override
    public String toString() {
        return value;
    }
}

