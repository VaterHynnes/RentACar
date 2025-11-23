package de.rentacar.booking.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Unit-Tests für AvailabilityService (kritisch für Überbuchungsverhinderung)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AvailabilityService Tests")
class AvailabilityServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private AvailabilityService availabilityService;

    private LocalDate tomorrow;
    private LocalDate nextWeek;

    @BeforeEach
    void setUp() {
        tomorrow = LocalDate.now().plusDays(1);
        nextWeek = LocalDate.now().plusDays(7);
    }

    @Test
    @DisplayName("Sollte true zurückgeben wenn keine überlappenden Buchungen existieren")
    void shouldReturnTrueWhenNoOverlappingBookings() {
        // Given
        when(bookingRepository.findOverlappingBookings(1L, tomorrow, nextWeek))
                .thenReturn(Collections.emptyList());

        // When
        boolean available = availabilityService.isVehicleAvailable(1L, tomorrow, nextWeek);

        // Then
        assertThat(available).isTrue();
    }

    @Test
    @DisplayName("Sollte false zurückgeben wenn überlappende Buchungen existieren")
    void shouldReturnFalseWhenOverlappingBookingsExist() {
        // Given
        Booking overlappingBooking = Booking.builder()
                .status(BookingStatus.BESTÄTIGT)
                .pickupDate(tomorrow.plusDays(2))
                .returnDate(nextWeek.minusDays(1))
                .build();
        overlappingBooking.setId(1L);

        when(bookingRepository.findOverlappingBookings(1L, tomorrow, nextWeek))
                .thenReturn(List.of(overlappingBooking));

        // When
        boolean available = availabilityService.isVehicleAvailable(1L, tomorrow, nextWeek);

        // Then
        assertThat(available).isFalse();
    }

    @Test
    @DisplayName("Sollte false zurückgeben wenn Buchung genau überlappt")
    void shouldReturnFalseWhenBookingExactlyOverlaps() {
        // Given
        Booking overlappingBooking = Booking.builder()
                .status(BookingStatus.BESTÄTIGT)
                .pickupDate(tomorrow)
                .returnDate(nextWeek)
                .build();
        overlappingBooking.setId(1L);

        when(bookingRepository.findOverlappingBookings(1L, tomorrow, nextWeek))
                .thenReturn(List.of(overlappingBooking));

        // When
        boolean available = availabilityService.isVehicleAvailable(1L, tomorrow, nextWeek);

        // Then
        assertThat(available).isFalse();
    }

    @Test
    @DisplayName("Sollte false zurückgeben wenn Buchung teilweise überlappt (Start)")
    void shouldReturnFalseWhenBookingPartiallyOverlapsAtStart() {
        // Given
        Booking overlappingBooking = Booking.builder()
                .status(BookingStatus.BESTÄTIGT)
                .pickupDate(tomorrow.minusDays(2))
                .returnDate(tomorrow.plusDays(2))
                .build();
        overlappingBooking.setId(1L);

        when(bookingRepository.findOverlappingBookings(1L, tomorrow, nextWeek))
                .thenReturn(List.of(overlappingBooking));

        // When
        boolean available = availabilityService.isVehicleAvailable(1L, tomorrow, nextWeek);

        // Then
        assertThat(available).isFalse();
    }

    @Test
    @DisplayName("Sollte false zurückgeben wenn Buchung teilweise überlappt (Ende)")
    void shouldReturnFalseWhenBookingPartiallyOverlapsAtEnd() {
        // Given
        Booking overlappingBooking = Booking.builder()
                .status(BookingStatus.BESTÄTIGT)
                .pickupDate(nextWeek.minusDays(2))
                .returnDate(nextWeek.plusDays(2))
                .build();
        overlappingBooking.setId(1L);

        when(bookingRepository.findOverlappingBookings(1L, tomorrow, nextWeek))
                .thenReturn(List.of(overlappingBooking));

        // When
        boolean available = availabilityService.isVehicleAvailable(1L, tomorrow, nextWeek);

        // Then
        assertThat(available).isFalse();
    }
}

