package de.rentacar.customer.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Value Object für verschlüsselte Strings (DSGVO-konform)
 */
@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class EncryptedString {

    @Column(name = "encrypted_value", nullable = false, length = 500)
    private String encryptedValue;

    public static EncryptedString of(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Wert darf nicht leer sein");
        }
        return new EncryptedString(value);
    }

    @Override
    public String toString() {
        return "[ENCRYPTED]";
    }
}

