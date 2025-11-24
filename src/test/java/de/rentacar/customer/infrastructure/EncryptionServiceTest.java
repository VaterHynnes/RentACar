package de.rentacar.customer.infrastructure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

/**
 * Unit-Tests für EncryptionService
 */
@DisplayName("EncryptionService Tests")
class EncryptionServiceTest {

    private EncryptionService encryptionService;

    @BeforeEach
    void setUp() {
        encryptionService = new EncryptionService("test-password");
    }

    @Test
    @DisplayName("Sollte Text verschlüsseln können")
    void shouldEncryptText() {
        // Given
        String plainText = "test@example.com";

        // When
        String encrypted = encryptionService.encrypt(plainText);

        // Then
        assertThat(encrypted).isNotNull();
        assertThat(encrypted).isNotEqualTo(plainText);
        assertThat(encrypted).isNotEmpty();
    }

    @Test
    @DisplayName("Sollte verschlüsselten Text entschlüsseln können")
    void shouldDecryptText() {
        // Given
        String plainText = "test@example.com";
        String encrypted = encryptionService.encrypt(plainText);

        // When
        String decrypted = encryptionService.decrypt(encrypted);

        // Then
        assertThat(decrypted).isEqualTo(plainText);
    }

    @Test
    @DisplayName("Sollte null zurückgeben wenn null verschlüsselt wird")
    void shouldReturnNullWhenEncryptingNull() {
        // When
        String result = encryptionService.encrypt(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Sollte null zurückgeben wenn null entschlüsselt wird")
    void shouldReturnNullWhenDecryptingNull() {
        // When
        String result = encryptionService.decrypt(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Sollte verschiedene Texte unterschiedlich verschlüsseln")
    void shouldEncryptDifferentTextsDifferently() {
        // Given
        String text1 = "test1@example.com";
        String text2 = "test2@example.com";

        // When
        String encrypted1 = encryptionService.encrypt(text1);
        String encrypted2 = encryptionService.encrypt(text2);

        // Then
        assertThat(encrypted1).isNotEqualTo(encrypted2);
    }

    @Test
    @DisplayName("Sollte gleichen Text unterschiedlich verschlüsseln (Salt)")
    void shouldEncryptSameTextDifferently() {
        // Given
        String text = "test@example.com";

        // When
        String encrypted1 = encryptionService.encrypt(text);
        String encrypted2 = encryptionService.encrypt(text);

        // Then
        assertThat(encrypted1).isNotEqualTo(encrypted2);
        
        // But both should decrypt to the same value
        assertThat(encryptionService.decrypt(encrypted1)).isEqualTo(text);
        assertThat(encryptionService.decrypt(encrypted2)).isEqualTo(text);
    }
}

