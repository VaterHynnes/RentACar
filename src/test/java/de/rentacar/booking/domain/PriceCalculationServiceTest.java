package de.rentacar.booking.domain;

import de.rentacar.vehicle.domain.VehicleType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit-Tests für PriceCalculationService
 */
@DisplayName("PriceCalculationService Tests")
class PriceCalculationServiceTest {

    private PriceCalculationService priceCalculationService;
    private LocalDate tomorrow;
    private LocalDate nextWeek;

    @BeforeEach
    void setUp() {
        priceCalculationService = new PriceCalculationService();
        tomorrow = LocalDate.now().plusDays(1);
        nextWeek = LocalDate.now().plusDays(7);
    }

    @Test
    @DisplayName("Sollte Preis für Kleinwagen korrekt berechnen")
    void shouldCalculatePriceForKleinwagen() {
        // When
        BigDecimal price = priceCalculationService.calculateTotalPrice(
                VehicleType.KLEINWAGEN, tomorrow, nextWeek);

        // Then (7 Tage * 30 EUR = 210 EUR)
        assertThat(price).isEqualByComparingTo(BigDecimal.valueOf(210.00));
    }

    @Test
    @DisplayName("Sollte Preis für Mittelklasse korrekt berechnen")
    void shouldCalculatePriceForMittelklasse() {
        // When
        BigDecimal price = priceCalculationService.calculateTotalPrice(
                VehicleType.MITTELKLASSE, tomorrow, nextWeek);

        // Then (7 Tage * 60 EUR = 420 EUR)
        assertThat(price).isEqualByComparingTo(BigDecimal.valueOf(420.00));
    }

    @Test
    @DisplayName("Sollte Preis für einen Tag korrekt berechnen")
    void shouldCalculatePriceForOneDay() {
        // When
        BigDecimal price = priceCalculationService.calculateTotalPrice(
                VehicleType.KLEINWAGEN, tomorrow, tomorrow);

        // Then (1 Tag * 30 EUR = 30 EUR)
        assertThat(price).isEqualByComparingTo(BigDecimal.valueOf(30.00));
    }

    @Test
    @DisplayName("Sollte Exception werfen wenn Abholdatum nach Rückgabedatum")
    void shouldThrowExceptionWhenPickupDateAfterReturnDate() {
        // When/Then
        assertThatThrownBy(() -> priceCalculationService.calculateTotalPrice(
                VehicleType.KLEINWAGEN, nextWeek, tomorrow))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("vor Rückgabedatum");
    }
}

