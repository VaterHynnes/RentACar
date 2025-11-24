package de.rentacar.booking.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit-Tests für Booking Aggregate
 */
@DisplayName("Booking Aggregate Tests")
class BookingTest {

    private Booking booking;
    private LocalDate tomorrow;
    private LocalDate nextWeek;

    @BeforeEach
    void setUp() {
        tomorrow = LocalDate.now().plusDays(1);
        nextWeek = LocalDate.now().plusDays(7);

        booking = Booking.builder()
                .customerId(1L)
                .pickupDate(tomorrow)
                .returnDate(nextWeek)
                .status(BookingStatus.ANFRAGE)
                .totalPrice(BigDecimal.valueOf(420.00))
                .build();
        booking.setId(1L);
    }

    @Test
    @DisplayName("Sollte Buchung bestätigen können")
    void shouldConfirmBooking() {
        // When
        booking.confirm();

        // Then
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.BESTÄTIGT);
    }

    @Test
    @DisplayName("Sollte Exception werfen wenn bereits bestätigte Buchung bestätigt wird")
    void shouldThrowExceptionWhenConfirmingAlreadyConfirmedBooking() {
        // Given
        booking.confirm();

        // When/Then
        assertThatThrownBy(() -> booking.confirm())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Nur Anfragen");
    }

    @Test
    @DisplayName("Sollte Buchung stornieren können wenn mehr als 24h vor Abholung")
    void shouldCancelBookingWhenMoreThan24HoursBeforePickup() {
        // Given
        LocalDate futureDate = LocalDate.now().plusDays(2);
        booking.setPickupDate(futureDate);
        booking.confirm();

        // When
        booking.cancel();

        // Then
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.STORNIERT);
        assertThat(booking.getCancellationDate()).isNotNull();
    }

    @Test
    @DisplayName("Sollte Exception werfen wenn Stornierung weniger als 24h vor Abholung")
    void shouldThrowExceptionWhenCancellingLessThan24HoursBeforePickup() {
        // Given
        LocalDate verySoon = LocalDate.now().plusDays(1);
        booking.setPickupDate(verySoon);
        booking.confirm();

        // When/Then
        assertThatThrownBy(() -> booking.cancel())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("24 Stunden");
    }

    @Test
    @DisplayName("Sollte true zurückgeben wenn Buchung aktiv ist")
    void shouldReturnTrueWhenBookingIsActive() {
        // Given
        booking.setStatus(BookingStatus.BESTÄTIGT);

        // When
        boolean active = booking.isActive();

        // Then
        assertThat(active).isTrue();
    }

    @Test
    @DisplayName("Sollte false zurückgeben wenn Buchung storniert ist")
    void shouldReturnFalseWhenBookingIsCancelled() {
        // Given
        booking.setStatus(BookingStatus.STORNIERT);

        // When
        boolean active = booking.isActive();

        // Then
        assertThat(active).isFalse();
    }

    @Test
    @DisplayName("Sollte true zurückgeben wenn Buchung überlappt")
    void shouldReturnTrueWhenBookingOverlaps() {
        // Given
        booking.setStatus(BookingStatus.BESTÄTIGT);
        LocalDate overlapStart = tomorrow.plusDays(2);
        LocalDate overlapEnd = nextWeek.minusDays(1);

        // When
        boolean overlaps = booking.overlapsWith(overlapStart, overlapEnd);

        // Then
        assertThat(overlaps).isTrue();
    }

    @Test
    @DisplayName("Sollte false zurückgeben wenn Buchung nicht überlappt")
    void shouldReturnFalseWhenBookingDoesNotOverlap() {
        // Given
        booking.setStatus(BookingStatus.BESTÄTIGT);
        LocalDate noOverlapStart = nextWeek.plusDays(1);
        LocalDate noOverlapEnd = nextWeek.plusDays(7);

        // When
        boolean overlaps = booking.overlapsWith(noOverlapStart, noOverlapEnd);

        // Then
        assertThat(overlaps).isFalse();
    }

    @Test
    @DisplayName("Sollte Buchung abschließen können")
    void shouldCompleteBooking() {
        // Given
        booking.setStatus(BookingStatus.BESTÄTIGT);

        // When
        booking.complete();

        // Then
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.ABGESCHLOSSEN);
    }

    @Test
    @DisplayName("Sollte Exception werfen wenn nicht bestätigte Buchung abgeschlossen wird")
    void shouldThrowExceptionWhenCompletingNonConfirmedBooking() {
        // Given
        booking.setStatus(BookingStatus.ANFRAGE);

        // When/Then
        assertThatThrownBy(() -> booking.complete())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("bestätigte Buchungen");
    }

    @Test
    @DisplayName("Sollte false zurückgeben wenn Buchung abgeschlossen ist")
    void shouldReturnFalseWhenBookingIsCompleted() {
        // Given
        booking.setStatus(BookingStatus.ABGESCHLOSSEN);

        // When
        boolean active = booking.isActive();

        // Then
        assertThat(active).isFalse();
    }

    @Test
    @DisplayName("Sollte false zurückgeben wenn Buchung nicht bestätigt ist für Overlap")
    void shouldReturnFalseWhenBookingNotConfirmedForOverlap() {
        // Given
        booking.setStatus(BookingStatus.ANFRAGE);
        LocalDate overlapStart = tomorrow.plusDays(2);
        LocalDate overlapEnd = nextWeek.minusDays(1);

        // When
        boolean overlaps = booking.overlapsWith(overlapStart, overlapEnd);

        // Then
        assertThat(overlaps).isFalse();
    }
}

