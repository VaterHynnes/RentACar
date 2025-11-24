package de.rentacar.rental.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit-Tests für Rental Aggregate
 */
@DisplayName("Rental Aggregate Tests")
class RentalTest {

    private Rental rental;

    @BeforeEach
    void setUp() {
        rental = Rental.builder()
                .bookingId(1L)
                .vehicleId(1L)
                .customerId(1L)
                .plannedPickupDate(LocalDate.now().plusDays(1))
                .plannedReturnDate(LocalDate.now().plusDays(7))
                .pickupMileage(50000L)
                .status(RentalStatus.AUSGEGEBEN)
                .additionalCosts(BigDecimal.ZERO)
                .build();
    }

    @Test
    @DisplayName("Sollte Check-out durchführen können")
    void shouldPerformCheckout() {
        // When
        rental.performCheckout(50000L, "Gut");

        // Then
        assertThat(rental.getPickupMileage()).isEqualTo(50000L);
        assertThat(rental.getPickupCondition()).isEqualTo("Gut");
        assertThat(rental.getActualPickupTime()).isNotNull();
        assertThat(rental.getStatus()).isEqualTo(RentalStatus.AUSGEGEBEN);
    }

    @Test
    @DisplayName("Sollte Exception werfen wenn Kilometerstand null ist")
    void shouldThrowExceptionWhenMileageIsNull() {
        // When/Then
        assertThatThrownBy(() -> rental.performCheckout(null, "Gut"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("gültig sein");
    }

    @Test
    @DisplayName("Sollte Exception werfen wenn Zustand leer ist")
    void shouldThrowExceptionWhenConditionIsEmpty() {
        // When/Then
        assertThatThrownBy(() -> rental.performCheckout(50000L, ""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("angegeben werden");
    }

    @Test
    @DisplayName("Sollte Check-in durchführen können")
    void shouldPerformCheckin() {
        // Given
        rental.performCheckout(50000L, "Gut");

        // When
        rental.performCheckin(50100L, "Gut");

        // Then
        assertThat(rental.getReturnMileage()).isEqualTo(50100L);
        assertThat(rental.getReturnCondition()).isEqualTo("Gut");
        assertThat(rental.getActualReturnTime()).isNotNull();
        assertThat(rental.getStatus()).isEqualTo(RentalStatus.ZURÜCKGEKEHRT);
    }

    @Test
    @DisplayName("Sollte Exception werfen wenn Rückgabe-Kilometerstand kleiner ist")
    void shouldThrowExceptionWhenReturnMileageIsSmaller() {
        // Given
        rental.performCheckout(50000L, "Gut");

        // When/Then
        assertThatThrownBy(() -> rental.performCheckin(49000L, "Gut"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("größer");
    }

    @Test
    @DisplayName("Sollte Schaden registrieren können")
    void shouldRegisterDamage() {
        // Given
        BigDecimal repairCost = BigDecimal.valueOf(250.00);
        String description = "Kratzer an der Tür";

        // When
        rental.registerDamage(repairCost, description);

        // Then
        assertThat(rental.getStatus()).isEqualTo(RentalStatus.MIT_SCHADEN);
        assertThat(rental.getAdditionalCosts()).isEqualByComparingTo(repairCost);
        assertThat(rental.getAdditionalCostsDescription()).contains(description);
    }

    @Test
    @DisplayName("Sollte mehrere Schäden akkumulieren")
    void shouldAccumulateMultipleDamages() {
        // Given
        BigDecimal cost1 = BigDecimal.valueOf(100.00);
        BigDecimal cost2 = BigDecimal.valueOf(150.00);

        // When
        rental.registerDamage(cost1, "Schaden 1");
        rental.registerDamage(cost2, "Schaden 2");

        // Then
        assertThat(rental.getAdditionalCosts()).isEqualByComparingTo(BigDecimal.valueOf(250.00));
    }

    @Test
    @DisplayName("Sollte Verspätungsgebühr hinzufügen können")
    void shouldAddLateReturnFee() {
        // Given
        BigDecimal fee = BigDecimal.valueOf(50.00);
        String description = "Verspätung um 1 Tag";

        // When
        rental.addLateReturnFee(fee, description);

        // Then
        assertThat(rental.getAdditionalCosts()).isEqualByComparingTo(fee);
        assertThat(rental.getAdditionalCostsDescription()).contains(description);
    }

    @Test
    @DisplayName("Sollte Exception werfen wenn Reparaturkosten negativ sind")
    void shouldThrowExceptionWhenRepairCostIsNegative() {
        // When/Then
        assertThatThrownBy(() -> rental.registerDamage(BigDecimal.valueOf(-100), "Schaden"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("gültig sein");
    }

    @Test
    @DisplayName("Sollte Exception werfen wenn Verspätungsgebühr negativ ist")
    void shouldThrowExceptionWhenLateFeeIsNegative() {
        // When/Then
        assertThatThrownBy(() -> rental.addLateReturnFee(BigDecimal.valueOf(-50), "Verspätung"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("gültig sein");
    }

    @Test
    @DisplayName("Sollte mehrere Verspätungsgebühren akkumulieren")
    void shouldAccumulateMultipleLateFees() {
        // Given
        BigDecimal fee1 = BigDecimal.valueOf(50.00);
        BigDecimal fee2 = BigDecimal.valueOf(75.00);

        // When
        rental.addLateReturnFee(fee1, "Verspätung 1");
        rental.addLateReturnFee(fee2, "Verspätung 2");

        // Then
        assertThat(rental.getAdditionalCosts()).isEqualByComparingTo(BigDecimal.valueOf(125.00));
    }

    @Test
    @DisplayName("Sollte Schäden und Verspätungsgebühren kombinieren")
    void shouldCombineDamagesAndLateFees() {
        // Given
        BigDecimal damageCost = BigDecimal.valueOf(200.00);
        BigDecimal lateFee = BigDecimal.valueOf(50.00);

        // When
        rental.registerDamage(damageCost, "Schaden");
        rental.addLateReturnFee(lateFee, "Verspätung");

        // Then
        assertThat(rental.getAdditionalCosts()).isEqualByComparingTo(BigDecimal.valueOf(250.00));
    }

    @Test
    @DisplayName("Sollte Exception werfen wenn Check-in ohne Check-out")
    void shouldThrowExceptionWhenCheckinWithoutCheckout() {
        // Given - neues Rental ohne Check-out (pickupMileage ist null)
        Rental newRental = Rental.builder()
                .bookingId(1L)
                .vehicleId(1L)
                .customerId(1L)
                .plannedPickupDate(LocalDate.now().plusDays(1))
                .plannedReturnDate(LocalDate.now().plusDays(7))
                .status(RentalStatus.AUSGEGEBEN)
                .additionalCosts(BigDecimal.ZERO)
                .build();
        // pickupMileage ist null, da kein Check-out durchgeführt wurde
        
        // When/Then
        assertThatThrownBy(() -> newRental.performCheckin(50100L, "Gut"))
                .isInstanceOf(NullPointerException.class); // NullPointerException wenn pickupMileage null ist
    }

    @Test
    @DisplayName("Sollte Exception werfen wenn Rückgabe-Kilometerstand null ist")
    void shouldThrowExceptionWhenReturnMileageIsNull() {
        // Given
        rental.performCheckout(50000L, "Gut");

        // When/Then
        assertThatThrownBy(() -> rental.performCheckin(null, "Gut"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("größer");
    }

    @Test
    @DisplayName("Sollte Exception werfen wenn Rückgabe-Zustand null ist")
    void shouldThrowExceptionWhenReturnConditionIsNull() {
        // Given
        rental.performCheckout(50000L, "Gut");

        // When/Then
        assertThatThrownBy(() -> rental.performCheckin(50100L, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("angegeben werden");
    }
}

