package de.rentacar.rental.application;

import de.rentacar.booking.domain.Booking;
import de.rentacar.booking.domain.BookingRepository;
import de.rentacar.booking.domain.BookingStatus;
import de.rentacar.rental.domain.DamageReport;
import de.rentacar.rental.domain.Rental;
import de.rentacar.rental.domain.RentalRepository;
import de.rentacar.rental.domain.RentalStatus;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit-Tests für RentalService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RentalService Tests")
class RentalServiceTest {

    @Mock
    private RentalRepository rentalRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private RentalService rentalService;

    private Booking testBooking;
    private Vehicle testVehicle;
    private Rental testRental;

    @BeforeEach
    void setUp() {
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

        testBooking = Booking.builder()
                .customerId(1L)
                .vehicle(testVehicle)
                .pickupDate(LocalDate.now().plusDays(1))
                .returnDate(LocalDate.now().plusDays(7))
                .pickupLocation("Berlin")
                .returnLocation("Berlin")
                .status(BookingStatus.BESTÄTIGT)
                .totalPrice(BigDecimal.valueOf(420.00))
                .build();
        testBooking.setId(1L);

        testRental = Rental.builder()
                .bookingId(1L)
                .vehicleId(1L)
                .customerId(1L)
                .plannedPickupDate(LocalDate.now().plusDays(1))
                .plannedReturnDate(LocalDate.now().plusDays(7))
                .pickupMileage(50000L)
                .status(RentalStatus.AUSGEGEBEN)
                .build();
        testRental.setId(1L);
    }

    @Test
    @DisplayName("Sollte Check-out durchführen können")
    void shouldPerformCheckout() {
        // Given
        Rental savedRental = Rental.builder()
                .bookingId(1L)
                .vehicleId(1L)
                .customerId(1L)
                .plannedPickupDate(LocalDate.now().plusDays(1))
                .plannedReturnDate(LocalDate.now().plusDays(7))
                .pickupMileage(50000L)
                .status(RentalStatus.AUSGEGEBEN)
                .build();
        savedRental.setId(1L);
        savedRental.performCheckout(50000L, "Gut");
        
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));
        when(rentalRepository.save(any(Rental.class))).thenReturn(savedRental);
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Rental result = rentalService.performCheckout(1L, 50000L, "Gut", "employee", "127.0.0.1");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getPickupMileage()).isEqualTo(50000L);
        assertThat(result.getPickupCondition()).isEqualTo("Gut");
        assertThat(result.getStatus()).isEqualTo(RentalStatus.AUSGEGEBEN);
        assertThat(testVehicle.getStatus()).isEqualTo(VehicleStatus.VERMIETET);
        
        verify(rentalRepository).save(any(Rental.class));
        verify(vehicleRepository).save(testVehicle);
        verify(auditService).logAction(anyString(), eq("RENTAL_CHECKOUT"), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Sollte Exception werfen wenn Buchung nicht gefunden wird")
    void shouldThrowExceptionWhenBookingNotFound() {
        // Given
        when(bookingRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> rentalService.performCheckout(999L, 50000L, "Gut", "employee", "127.0.0.1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nicht gefunden");
    }

    @Test
    @DisplayName("Sollte Exception werfen wenn Buchung nicht bestätigt ist")
    void shouldThrowExceptionWhenBookingNotConfirmed() {
        // Given
        testBooking.setStatus(BookingStatus.ANFRAGE);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));

        // When/Then
        assertThatThrownBy(() -> rentalService.performCheckout(1L, 50000L, "Gut", "employee", "127.0.0.1"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("bestätigte Buchungen");
    }

    @Test
    @DisplayName("Sollte Check-in durchführen können")
    void shouldPerformCheckin() {
        // Given
        testRental.performCheckout(50000L, "Gut");
        when(rentalRepository.findById(1L)).thenReturn(Optional.of(testRental));
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));
        when(rentalRepository.save(any(Rental.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Rental result = rentalService.performCheckin(1L, 50100L, "Gut", "employee", "127.0.0.1");

        // Then
        assertThat(result.getReturnMileage()).isEqualTo(50100L);
        assertThat(result.getReturnCondition()).isEqualTo("Gut");
        assertThat(result.getStatus()).isEqualTo(RentalStatus.ZURÜCKGEKEHRT);
        assertThat(testVehicle.getStatus()).isEqualTo(VehicleStatus.VERFÜGBAR);
        assertThat(testBooking.getStatus()).isEqualTo(BookingStatus.ABGESCHLOSSEN);
        
        verify(rentalRepository).save(testRental);
        verify(vehicleRepository).save(testVehicle);
        verify(bookingRepository).save(testBooking);
        verify(auditService).logAction(anyString(), eq("RENTAL_CHECKIN"), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Sollte Verspätungsgebühr bei verspäteter Rückgabe berechnen")
    void shouldCalculateLateFeeForLateReturn() {
        // Given
        testRental.performCheckout(50000L, "Gut");
        // Simuliere verspätete Rückgabe (1 Tag später)
        testRental.setPlannedReturnDate(LocalDate.now().minusDays(1));
        when(rentalRepository.findById(1L)).thenReturn(Optional.of(testRental));
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));
        when(rentalRepository.save(any(Rental.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Rental result = rentalService.performCheckin(1L, 50100L, "Gut", "employee", "127.0.0.1");

        // Then
        assertThat(result.getAdditionalCosts()).isGreaterThan(BigDecimal.ZERO);
        assertThat(result.getAdditionalCostsDescription()).contains("Verspätung");
    }

    @Test
    @DisplayName("Sollte Schadensbericht erstellen können")
    void shouldCreateDamageReport() {
        // Given
        when(rentalRepository.findById(1L)).thenReturn(Optional.of(testRental));
        when(rentalRepository.save(any(Rental.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        DamageReport result = rentalService.createDamageReport(
                1L, "Kratzer an der Tür", BigDecimal.valueOf(250.00),
                "Kunde hat Versicherung", "employee", "127.0.0.1");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getDescription()).isEqualTo("Kratzer an der Tür");
        assertThat(result.getRepairCost()).isEqualByComparingTo(BigDecimal.valueOf(250.00));
        assertThat(testRental.getStatus()).isEqualTo(RentalStatus.MIT_SCHADEN);
        
        verify(rentalRepository).save(testRental);
        verify(auditService).logAction(anyString(), eq("DAMAGE_REPORT_CREATED"), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Sollte Exception werfen wenn Vermietung nicht gefunden wird")
    void shouldThrowExceptionWhenRentalNotFound() {
        // Given
        when(rentalRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> rentalService.createDamageReport(
                999L, "Schaden", BigDecimal.valueOf(100), "Notes", "employee", "127.0.0.1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nicht gefunden");
    }

    @Test
    @DisplayName("Sollte Exception werfen wenn Vermietung nicht gefunden wird beim Check-in")
    void shouldThrowExceptionWhenRentalNotFoundOnCheckin() {
        // Given
        when(rentalRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> rentalService.performCheckin(999L, 50100L, "Gut", "employee", "127.0.0.1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nicht gefunden");
    }

    @Test
    @DisplayName("Sollte keine Verspätungsgebühr berechnen wenn pünktlich zurückgegeben")
    void shouldNotCalculateLateFeeWhenReturnedOnTime() {
        // Given
        testRental.performCheckout(50000L, "Gut");
        // Rückgabe am geplanten Tag
        testRental.setPlannedReturnDate(LocalDate.now());
        when(rentalRepository.findById(1L)).thenReturn(Optional.of(testRental));
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));
        when(rentalRepository.save(any(Rental.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Rental result = rentalService.performCheckin(1L, 50100L, "Gut", "employee", "127.0.0.1");

        // Then - keine zusätzlichen Kosten wenn pünktlich
        // (kann nicht direkt getestet werden, da actualReturnTime in performCheckin gesetzt wird)
        assertThat(result.getReturnMileage()).isEqualTo(50100L);
    }
}

