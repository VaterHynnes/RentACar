package de.rentacar.customer.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit-Tests für Customer Aggregate
 */
@DisplayName("Customer Aggregate Tests")
class CustomerTest {

    private Customer customer;
    private EncryptedString email;
    private EncryptedString phone;
    private EncryptedString address;
    private EncryptedString license;

    @BeforeEach
    void setUp() {
        email = EncryptedString.of("encrypted-email");
        phone = EncryptedString.of("encrypted-phone");
        address = EncryptedString.of("encrypted-address");
        license = EncryptedString.of("encrypted-license");

        customer = Customer.builder()
                .username("testuser")
                .password("hashed-password")
                .firstName("Max")
                .lastName("Mustermann")
                .email(email)
                .phone(phone)
                .address(address)
                .driverLicenseNumber(license)
                .build();
    }

    @Test
    @DisplayName("Sollte Kundendaten aktualisieren können")
    void shouldUpdatePersonalData() {
        // Given
        EncryptedString newEmail = EncryptedString.of("new-encrypted-email");
        EncryptedString newPhone = EncryptedString.of("new-encrypted-phone");
        EncryptedString newAddress = EncryptedString.of("new-encrypted-address");

        // When
        customer.updatePersonalData("John", "Doe", newEmail, newPhone, newAddress);

        // Then
        assertThat(customer.getFirstName()).isEqualTo("John");
        assertThat(customer.getLastName()).isEqualTo("Doe");
        assertThat(customer.getEmail()).isEqualTo(newEmail);
        assertThat(customer.getPhone()).isEqualTo(newPhone);
        assertThat(customer.getAddress()).isEqualTo(newAddress);
    }

    @Test
    @DisplayName("Sollte nur geänderte Felder aktualisieren")
    void shouldUpdateOnlyChangedFields() {
        // Given
        String originalFirstName = customer.getFirstName();
        String originalLastName = customer.getLastName();

        // When
        customer.updatePersonalData(null, null, null, null, null);

        // Then
        assertThat(customer.getFirstName()).isEqualTo(originalFirstName);
        assertThat(customer.getLastName()).isEqualTo(originalLastName);
    }

    @Test
    @DisplayName("Sollte Führerscheinnummer aktualisieren können")
    void shouldUpdateDriverLicense() {
        // Given
        EncryptedString newLicense = EncryptedString.of("new-encrypted-license");

        // When
        customer.updateDriverLicense(newLicense);

        // Then
        assertThat(customer.getDriverLicenseNumber()).isEqualTo(newLicense);
    }

    @Test
    @DisplayName("Sollte Exception werfen wenn Führerscheinnummer null ist")
    void shouldThrowExceptionWhenDriverLicenseIsNull() {
        // When/Then
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> {
            customer.updateDriverLicense(null);
        });
    }

    @Test
    @DisplayName("Sollte nur geänderte Felder aktualisieren bei updatePersonalData")
    void shouldUpdateOnlyChangedFieldsInUpdatePersonalData() {
        // Given
        String originalFirstName = customer.getFirstName();
        String originalLastName = customer.getLastName();
        EncryptedString originalEmail = customer.getEmail();
        EncryptedString originalPhone = customer.getPhone();
        EncryptedString originalAddress = customer.getAddress();

        // When - nur firstName ändern
        customer.updatePersonalData("John", null, null, null, null);

        // Then
        assertThat(customer.getFirstName()).isEqualTo("John");
        assertThat(customer.getLastName()).isEqualTo(originalLastName);
        assertThat(customer.getEmail()).isEqualTo(originalEmail);
        assertThat(customer.getPhone()).isEqualTo(originalPhone);
        assertThat(customer.getAddress()).isEqualTo(originalAddress);
    }

    @Test
    @DisplayName("Sollte alle Felder aktualisieren können")
    void shouldUpdateAllFields() {
        // Given
        EncryptedString newEmail = EncryptedString.of("new-email");
        EncryptedString newPhone = EncryptedString.of("new-phone");
        EncryptedString newAddress = EncryptedString.of("new-address");

        // When
        customer.updatePersonalData("John", "Doe", newEmail, newPhone, newAddress);

        // Then
        assertThat(customer.getFirstName()).isEqualTo("John");
        assertThat(customer.getLastName()).isEqualTo("Doe");
        assertThat(customer.getEmail()).isEqualTo(newEmail);
        assertThat(customer.getPhone()).isEqualTo(newPhone);
        assertThat(customer.getAddress()).isEqualTo(newAddress);
    }
}

