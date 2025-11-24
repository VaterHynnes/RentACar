package de.rentacar.vehicle.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit-Tests für LicensePlate Value Object
 */
@DisplayName("LicensePlate Value Object Tests")
class LicensePlateTest {

    @Test
    @DisplayName("Sollte LicensePlate mit gültigem Wert erstellen")
    void shouldCreateLicensePlateWithValidValue() {
        // When
        LicensePlate plate = LicensePlate.of("B-AB 1234");

        // Then
        assertThat(plate.getValue()).isEqualTo("B-AB 1234");
    }

    @Test
    @DisplayName("Sollte LicensePlate in Großbuchstaben konvertieren")
    void shouldConvertToUpperCase() {
        // When
        LicensePlate plate = LicensePlate.of("b-ab 1234");

        // Then
        assertThat(plate.getValue()).isEqualTo("B-AB 1234");
    }

    @Test
    @DisplayName("Sollte Whitespace trimmen")
    void shouldTrimWhitespace() {
        // When
        LicensePlate plate = LicensePlate.of("  B-AB 1234  ");

        // Then
        assertThat(plate.getValue()).isEqualTo("B-AB 1234");
    }

    @Test
    @DisplayName("Sollte Exception werfen wenn Wert null ist")
    void shouldThrowExceptionWhenValueIsNull() {
        // When/Then
        assertThatThrownBy(() -> LicensePlate.of(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("darf nicht leer sein");
    }

    @Test
    @DisplayName("Sollte Exception werfen wenn Wert leer ist")
    void shouldThrowExceptionWhenValueIsEmpty() {
        // When/Then
        assertThatThrownBy(() -> LicensePlate.of(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("darf nicht leer sein");
    }

    @Test
    @DisplayName("Sollte Exception werfen wenn Wert nur Whitespace ist")
    void shouldThrowExceptionWhenValueIsWhitespace() {
        // When/Then
        assertThatThrownBy(() -> LicensePlate.of("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("darf nicht leer sein");
    }

    @Test
    @DisplayName("Sollte equals korrekt implementieren")
    void shouldImplementEqualsCorrectly() {
        // Given
        LicensePlate plate1 = LicensePlate.of("B-AB 1234");
        LicensePlate plate2 = LicensePlate.of("B-AB 1234");
        LicensePlate plate3 = LicensePlate.of("B-CD 5678");

        // Then
        assertThat(plate1).isEqualTo(plate2);
        assertThat(plate1).isNotEqualTo(plate3);
    }

    @Test
    @DisplayName("Sollte hashCode korrekt implementieren")
    void shouldImplementHashCodeCorrectly() {
        // Given
        LicensePlate plate1 = LicensePlate.of("B-AB 1234");
        LicensePlate plate2 = LicensePlate.of("B-AB 1234");

        // Then
        assertThat(plate1.hashCode()).isEqualTo(plate2.hashCode());
    }

    @Test
    @DisplayName("Sollte toString korrekt implementieren")
    void shouldImplementToStringCorrectly() {
        // Given
        LicensePlate plate = LicensePlate.of("B-AB 1234");

        // When
        String result = plate.toString();

        // Then
        assertThat(result).isEqualTo("B-AB 1234");
    }
}

