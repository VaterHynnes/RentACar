package de.rentacar.customer.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit-Tests für EncryptedString Value Object
 */
@DisplayName("EncryptedString Value Object Tests")
class EncryptedStringTest {

    @Test
    @DisplayName("Sollte EncryptedString mit gültigem Wert erstellen")
    void shouldCreateEncryptedStringWithValidValue() {
        // When
        EncryptedString encrypted = EncryptedString.of("encrypted-value");

        // Then
        assertThat(encrypted.getEncryptedValue()).isEqualTo("encrypted-value");
    }

    @Test
    @DisplayName("Sollte Exception werfen wenn Wert null ist")
    void shouldThrowExceptionWhenValueIsNull() {
        // When/Then
        assertThatThrownBy(() -> EncryptedString.of(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("darf nicht leer sein");
    }

    @Test
    @DisplayName("Sollte Exception werfen wenn Wert leer ist")
    void shouldThrowExceptionWhenValueIsEmpty() {
        // When/Then
        assertThatThrownBy(() -> EncryptedString.of(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("darf nicht leer sein");
    }

    @Test
    @DisplayName("Sollte Exception werfen wenn Wert nur Whitespace ist")
    void shouldThrowExceptionWhenValueIsWhitespace() {
        // When/Then
        assertThatThrownBy(() -> EncryptedString.of("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("darf nicht leer sein");
    }

    @Test
    @DisplayName("Sollte equals korrekt implementieren")
    void shouldImplementEqualsCorrectly() {
        // Given
        EncryptedString str1 = EncryptedString.of("encrypted-value");
        EncryptedString str2 = EncryptedString.of("encrypted-value");
        EncryptedString str3 = EncryptedString.of("different-value");

        // Then
        assertThat(str1).isEqualTo(str2);
        assertThat(str1).isNotEqualTo(str3);
    }

    @Test
    @DisplayName("Sollte hashCode korrekt implementieren")
    void shouldImplementHashCodeCorrectly() {
        // Given
        EncryptedString str1 = EncryptedString.of("encrypted-value");
        EncryptedString str2 = EncryptedString.of("encrypted-value");

        // Then
        assertThat(str1.hashCode()).isEqualTo(str2.hashCode());
    }

    @Test
    @DisplayName("Sollte toString [ENCRYPTED] zurückgeben")
    void shouldReturnEncryptedInToString() {
        // Given
        EncryptedString encrypted = EncryptedString.of("encrypted-value");

        // When
        String result = encrypted.toString();

        // Then
        assertThat(result).isEqualTo("[ENCRYPTED]");
    }
}

