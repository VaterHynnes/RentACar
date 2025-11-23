package de.rentacar.booking.application;

import de.rentacar.booking.domain.*;
import de.rentacar.customer.domain.Customer;
import de.rentacar.customer.domain.CustomerRepository;
import de.rentacar.shared.domain.AuditService;
import de.rentacar.vehicle.domain.LicensePlate;
import de.rentacar.vehicle.domain.Vehicle;
import de.rentacar.vehicle.domain.VehicleRepository;
import de.rentacar.vehicle.domain.VehicleStatus;
import de.rentacar.vehicle.domain.VehicleType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit-Tests für BookingService (kritischer Domain Service)
 * Testabdeckung: Verfügbarkeitsprüfung, Überbuchungsverhinderung, Preisberechnung
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BookingService Tests")
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private PriceCalculationService priceCalculationService;

    @Mock
    private AvailabilityService availabilityService;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private BookingService bookingService;

    private Vehicle testVehicle;
    private Customer testCustomer;
    private LocalDate tomorrow;
    private LocalDate nextWeek;

    @BeforeEach
    void setUp() {
        tomorrow = LocalDate.now().plusDays(1);
        nextWeek = LocalDate.now().plusDays(7);

        testVehicle = Vehicle.builder()
                .licensePlate(LicensePlate.of("B-AB 1234"))
                .brand("BMW")
                .model("320d")
                .type(VehicleType.MITTELKLASSE)
                .mileage(50000L)
                .location("Berlin")
                .status(VehicleStatus.VERFÜGBAR)
                .dailyPrice(60.0)
                .build();
        testVehicle.setId(1L);

        testCustomer = Customer.builder()
                .username("testuser")
                .firstName("Max")
                .lastName("Mustermann")
                .build();
        testCustomer.setId(1L);
    }

    @Test
    @DisplayName("Sollte verfügbare Fahrzeuge suchen können")
    void shouldSearchAvailableVehicles() {
        // Given
        when(vehicleRepository.findAvailableVehicles(
                VehicleType.MITTELKLASSE, "Berlin", tomorrow, nextWeek))
                .thenReturn(List.of(testVehicle));

        // When
        List<Vehicle> result = bookingService.searchAvailableVehicles(
                VehicleType.MITTELKLASSE, "Berlin", tomorrow, nextWeek);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(testVehicle);
        verify(vehicleRepository).findAvailableVehicles(
                VehicleType.MITTELKLASSE, "Berlin", tomorrow, nextWeek);
    }

    @Test
    @DisplayName("Sollte Buchung erstellen wenn Fahrzeug verfügbar ist")
    void shouldCreateBookingWhenVehicleIsAvailable() {
        // Given
        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));
        when(availabilityService.isVehicleAvailable(1L, tomorrow, nextWeek)).thenReturn(true);
        when(priceCalculationService.calculateTotalPrice(
                VehicleType.MITTELKLASSE, tomorrow, nextWeek))
                .thenReturn(BigDecimal.valueOf(420.00));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Booking booking = bookingService.createBooking(
                1L, 1L, tomorrow, nextWeek, "Berlin", "Berlin", "testuser", "127.0.0.1");

        // Then
        assertThat(booking).isNotNull();
        assertThat(booking.getCustomerId()).isEqualTo(1L);
        assertThat(booking.getVehicle()).isNotNull();
        assertThat(booking.getVehicle().getId()).isEqualTo(1L);
        assertThat(booking.getPickupDate()).isEqualTo(tomorrow);
        assertThat(booking.getReturnDate()).isEqualTo(nextWeek);
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.ANFRAGE);
        assertThat(booking.getTotalPrice()).isEqualByComparingTo(BigDecimal.valueOf(420.00));

        verify(availabilityService).isVehicleAvailable(1L, tomorrow, nextWeek);
        verify(bookingRepository).save(any(Booking.class));
        verify(auditService).logAction(anyString(), eq("BOOKING_CREATED"), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Sollte Exception werfen wenn Fahrzeug nicht verfügbar ist (Überbuchung verhindern)")
    void shouldThrowExceptionWhenVehicleNotAvailable() {
        // Given
        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));
        when(availabilityService.isVehicleAvailable(1L, tomorrow, nextWeek)).thenReturn(false);

        // When/Then
        assertThatThrownBy(() -> bookingService.createBooking(
                1L, 1L, tomorrow, nextWeek, "Berlin", "Berlin", "testuser", "127.0.0.1"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nicht verfügbar");

        verify(availabilityService).isVehicleAvailable(1L, tomorrow, nextWeek);
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    @DisplayName("Sollte Exception werfen wenn Kunde nicht existiert")
    void shouldThrowExceptionWhenCustomerNotFound() {
        // Given
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> bookingService.createBooking(
                1L, 1L, tomorrow, nextWeek, "Berlin", "Berlin", "testuser", "127.0.0.1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Kunde nicht gefunden");

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    @DisplayName("Sollte Exception werfen wenn Fahrzeug nicht existiert")
    void shouldThrowExceptionWhenVehicleNotFound() {
        // Given
        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(vehicleRepository.findById(1L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> bookingService.createBooking(
                1L, 1L, tomorrow, nextWeek, "Berlin", "Berlin", "testuser", "127.0.0.1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Fahrzeug nicht gefunden");

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    @DisplayName("Sollte Exception werfen wenn Abholdatum in der Vergangenheit liegt")
    void shouldThrowExceptionWhenPickupDateInPast() {
        // Given
        LocalDate yesterday = LocalDate.now().minusDays(1);

        // When/Then
        assertThatThrownBy(() -> bookingService.createBooking(
                1L, 1L, yesterday, nextWeek, "Berlin", "Berlin", "testuser", "127.0.0.1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Vergangenheit");
    }

    @Test
    @DisplayName("Sollte Exception werfen wenn Abholdatum nach Rückgabedatum liegt")
    void shouldThrowExceptionWhenPickupDateAfterReturnDate() {
        // When/Then
        assertThatThrownBy(() -> bookingService.createBooking(
                1L, 1L, nextWeek, tomorrow, "Berlin", "Berlin", "testuser", "127.0.0.1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("vor Rückgabedatum");
    }

    @Test
    @DisplayName("Sollte Buchung bestätigen können wenn verfügbar")
    void shouldConfirmBookingWhenAvailable() {
        // Given
        Booking booking = Booking.builder()
                .customerId(1L)
                .vehicle(testVehicle)
                .pickupDate(tomorrow)
                .returnDate(nextWeek)
                .status(BookingStatus.ANFRAGE)
                .totalPrice(BigDecimal.valueOf(420.00))
                .build();
        booking.setId(1L);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(availabilityService.isVehicleAvailable(1L, tomorrow, nextWeek)).thenReturn(true);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        bookingService.confirmBooking(1L, "testuser", "127.0.0.1");

        // Then
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.BESTÄTIGT);
        assertThat(booking.getVehicle().getStatus()).isEqualTo(VehicleStatus.VERMIETET);
        verify(bookingRepository).save(booking);
        verify(vehicleRepository).save(testVehicle);
        verify(auditService).logAction(anyString(), eq("BOOKING_CONFIRMED"), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Sollte Exception werfen wenn Fahrzeug bei Bestätigung nicht mehr verfügbar ist")
    void shouldThrowExceptionWhenVehicleNotAvailableOnConfirm() {
        // Given
        Booking booking = Booking.builder()
                .customerId(1L)
                .vehicle(testVehicle)
                .pickupDate(tomorrow)
                .returnDate(nextWeek)
                .status(BookingStatus.ANFRAGE)
                .build();
        booking.setId(1L);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(availabilityService.isVehicleAvailable(1L, tomorrow, nextWeek)).thenReturn(false);

        // When/Then
        assertThatThrownBy(() -> bookingService.confirmBooking(1L, "testuser", "127.0.0.1"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nicht mehr verfügbar");

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    @DisplayName("Sollte Buchung stornieren können bis 24h vor Abholung")
    void shouldCancelBookingBefore24Hours() {
        // Given
        LocalDate futureDate = LocalDate.now().plusDays(2); // Mehr als 24h in der Zukunft
        Booking booking = Booking.builder()
                .customerId(1L)
                .vehicle(testVehicle)
                .pickupDate(futureDate)
                .returnDate(nextWeek)
                .status(BookingStatus.BESTÄTIGT)
                .build();
        booking.setId(1L);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        bookingService.cancelBooking(1L, "testuser", "127.0.0.1");

        // Then
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.STORNIERT);
        assertThat(booking.getCancellationDate()).isNotNull();
        assertThat(booking.getVehicle().getStatus()).isEqualTo(VehicleStatus.VERFÜGBAR);
        verify(bookingRepository).save(booking);
        verify(vehicleRepository).save(testVehicle);
        verify(auditService).logAction(anyString(), eq("BOOKING_CANCELLED"), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Sollte Buchungshistorie für Kunde zurückgeben")
    void shouldReturnBookingHistoryForCustomer() {
        // Given
        Booking booking1 = Booking.builder().customerId(1L).build();
        booking1.setId(1L);
        Booking booking2 = Booking.builder().customerId(1L).build();
        booking2.setId(2L);

        when(bookingRepository.findByCustomerId(1L)).thenReturn(List.of(booking1, booking2));

        // When
        List<Booking> result = bookingService.getBookingHistory(1L);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(booking1, booking2);
        verify(bookingRepository).findByCustomerId(1L);
    }
}

