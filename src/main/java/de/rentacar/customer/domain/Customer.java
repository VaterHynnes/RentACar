package de.rentacar.customer.domain;

import de.rentacar.shared.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Aggregate Root für Kunden (Customer Context)
 */
@Entity
@Table(name = "customers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String firstName;

    @Column(nullable = false, length = 100)
    private String lastName;

    @Embedded
    @AttributeOverride(name = "encryptedValue", column = @Column(name = "encrypted_email"))
    private EncryptedString email;

    @Embedded
    @AttributeOverride(name = "encryptedValue", column = @Column(name = "encrypted_phone"))
    private EncryptedString phone;

    @Embedded
    @AttributeOverride(name = "encryptedValue", column = @Column(name = "encrypted_address"))
    private EncryptedString address;

    @Embedded
    @AttributeOverride(name = "encryptedValue", column = @Column(name = "encrypted_license_number"))
    private EncryptedString driverLicenseNumber;

    @Column(nullable = false, unique = true)
    private String username; // Für Login

    @Column(nullable = false)
    private String password; // Sollte gehasht sein (BCrypt)

    /**
     * Domain-Methode: Aktualisiert Kundendaten
     */
    public void updatePersonalData(String firstName, String lastName, EncryptedString email, 
                                   EncryptedString phone, EncryptedString address) {
        if (firstName != null && !firstName.trim().isEmpty()) {
            this.firstName = firstName;
        }
        if (lastName != null && !lastName.trim().isEmpty()) {
            this.lastName = lastName;
        }
        if (email != null) {
            this.email = email;
        }
        if (phone != null) {
            this.phone = phone;
        }
        if (address != null) {
            this.address = address;
        }
    }

    /**
     * Domain-Methode: Aktualisiert Führerscheinnummer
     */
    public void updateDriverLicense(EncryptedString driverLicenseNumber) {
        if (driverLicenseNumber == null) {
            throw new IllegalArgumentException("Führerscheinnummer darf nicht null sein");
        }
        this.driverLicenseNumber = driverLicenseNumber;
    }
}

