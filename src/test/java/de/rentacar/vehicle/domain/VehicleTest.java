package de.rentacar.vehicle.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit-Tests für Vehicle Aggregate
 */
@DisplayName("Vehicle Aggregate Tests")
class VehicleTest {

    private Vehicle vehicle;

    @BeforeEach
    void setUp() {
        vehicle = Vehicle.builder()
                .licensePlate(LicensePlate.of("B-AB 1234"))
                .brand("BMW")
                .model("320d")
                .type(VehicleType.MITTELKLASSE)
                .mileage(50000L)
                .location("Berlin")
                .status(VehicleStatus.VERFÜGBAR)
                .dailyPrice(60.0)
                .build();
    }

    @Test
    @DisplayName("Sollte Fahrzeug als vermietet markieren können")
    void shouldMarkAsRented() {
        // When
        vehicle.markAsRented();

        // Then
        assertThat(vehicle.getStatus()).isEqualTo(VehicleStatus.VERMIETET);
    }

    @Test
    @DisplayName("Sollte Exception werfen wenn nicht verfügbares Fahrzeug vermietet wird")
    void shouldThrowExceptionWhenRentingUnavailableVehicle() {
        // Given
        vehicle.setStatus(VehicleStatus.WARTUNG);

        // When/Then
        assertThatThrownBy(() -> vehicle.markAsRented())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nicht verfügbar");
    }

    @Test
    @DisplayName("Sollte Fahrzeug als verfügbar markieren können")
    void shouldMarkAsAvailable() {
        // Given
        vehicle.setStatus(VehicleStatus.VERMIETET);

        // When
        vehicle.markAsAvailable();

        // Then
        assertThat(vehicle.getStatus()).isEqualTo(VehicleStatus.VERFÜGBAR);
    }

    @Test
    @DisplayName("Sollte Fahrzeug in Wartung setzen können")
    void shouldMarkAsMaintenance() {
        // When
        vehicle.markAsMaintenance();

        // Then
        assertThat(vehicle.getStatus()).isEqualTo(VehicleStatus.WARTUNG);
    }

    @Test
    @DisplayName("Sollte Fahrzeug außer Betrieb setzen können")
    void shouldMarkAsOutOfService() {
        // When
        vehicle.markAsOutOfService();

        // Then
        assertThat(vehicle.getStatus()).isEqualTo(VehicleStatus.AUSSER_BETRIEB);
    }

    @Test
    @DisplayName("Sollte Kilometerstand aktualisieren können")
    void shouldUpdateMileage() {
        // When
        vehicle.updateMileage(51000L);

        // Then
        assertThat(vehicle.getMileage()).isEqualTo(51000L);
    }

    @Test
    @DisplayName("Sollte Exception werfen wenn neuer Kilometerstand kleiner ist")
    void shouldThrowExceptionWhenNewMileageIsSmaller() {
        // When/Then
        assertThatThrownBy(() -> vehicle.updateMileage(49000L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("größer");
    }

    @Test
    @DisplayName("Sollte Exception werfen wenn neuer Kilometerstand null ist")
    void shouldThrowExceptionWhenNewMileageIsNull() {
        // When/Then
        assertThatThrownBy(() -> vehicle.updateMileage(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("größer");
    }

    @Test
    @DisplayName("Sollte true zurückgeben wenn Fahrzeug verfügbar ist")
    void shouldReturnTrueWhenVehicleIsAvailable() {
        // Given
        vehicle.setStatus(VehicleStatus.VERFÜGBAR);

        // When
        boolean available = vehicle.isAvailable();

        // Then
        assertThat(available).isTrue();
    }

    @Test
    @DisplayName("Sollte false zurückgeben wenn Fahrzeug nicht verfügbar ist")
    void shouldReturnFalseWhenVehicleIsNotAvailable() {
        // Given
        vehicle.setStatus(VehicleStatus.VERMIETET);

        // When
        boolean available = vehicle.isAvailable();

        // Then
        assertThat(available).isFalse();
    }

    @Test
    @DisplayName("Sollte false zurückgeben wenn Fahrzeug in Wartung ist")
    void shouldReturnFalseWhenVehicleIsInMaintenance() {
        // Given
        vehicle.setStatus(VehicleStatus.WARTUNG);

        // When
        boolean available = vehicle.isAvailable();

        // Then
        assertThat(available).isFalse();
    }

    @Test
    @DisplayName("Sollte false zurückgeben wenn Fahrzeug außer Betrieb ist")
    void shouldReturnFalseWhenVehicleIsOutOfService() {
        // Given
        vehicle.setStatus(VehicleStatus.AUSSER_BETRIEB);

        // When
        boolean available = vehicle.isAvailable();

        // Then
        assertThat(available).isFalse();
    }

    @Test
    @DisplayName("Sollte Kilometerstand gleich dem aktuellen setzen können")
    void shouldAllowSettingMileageEqualToCurrent() {
        // Given
        Long currentMileage = vehicle.getMileage();

        // When
        vehicle.updateMileage(currentMileage);

        // Then
        assertThat(vehicle.getMileage()).isEqualTo(currentMileage);
    }
}

