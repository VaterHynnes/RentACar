package de.rentacar.vehicle.application;

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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit-Tests für VehicleManagementService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("VehicleManagementService Tests")
class VehicleManagementServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private VehicleManagementService vehicleManagementService;

    private Vehicle testVehicle;

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
    }

    @Test
    @DisplayName("Sollte Fahrzeug hinzufügen können")
    void shouldAddVehicle() {
        // Given
        Vehicle savedVehicle = Vehicle.builder()
                .licensePlate(LicensePlate.of("B-TEST 9999"))
                .brand("Audi")
                .model("A4")
                .type(VehicleType.MITTELKLASSE)
                .mileage(10000L)
                .location("Berlin")
                .status(VehicleStatus.VERFÜGBAR)
                .dailyPrice(70.0)
                .build();
        savedVehicle.setId(1L);
        
        when(vehicleRepository.findByLicensePlate(any(LicensePlate.class))).thenReturn(Optional.empty());
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(savedVehicle);

        // When
        Vehicle result = vehicleManagementService.addVehicle(
                "B-TEST 9999", "Audi", "A4", VehicleType.MITTELKLASSE,
                10000L, "Berlin", 70.0, "employee", "127.0.0.1");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getBrand()).isEqualTo("Audi");
        assertThat(result.getModel()).isEqualTo("A4");
        assertThat(result.getStatus()).isEqualTo(VehicleStatus.VERFÜGBAR);
        
        verify(vehicleRepository).findByLicensePlate(any(LicensePlate.class));
        verify(vehicleRepository).save(any(Vehicle.class));
        verify(auditService).logAction(anyString(), eq("VEHICLE_ADDED"), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Sollte Exception werfen wenn Kennzeichen bereits existiert")
    void shouldThrowExceptionWhenLicensePlateExists() {
        // Given
        when(vehicleRepository.findByLicensePlate(any(LicensePlate.class)))
                .thenReturn(Optional.of(testVehicle));

        // When/Then
        assertThatThrownBy(() -> vehicleManagementService.addVehicle(
                "B-AB 1234", "BMW", "320d", VehicleType.MITTELKLASSE,
                50000L, "Berlin", 60.0, "employee", "127.0.0.1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("existiert bereits");

        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    @DisplayName("Sollte Fahrzeug aktualisieren können")
    void shouldUpdateVehicle() {
        // Given
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Vehicle result = vehicleManagementService.updateVehicle(
                1L, "Audi", "A4", VehicleType.OBERKLASSE, "München", 80.0,
                "employee", "127.0.0.1");

        // Then
        assertThat(result.getBrand()).isEqualTo("Audi");
        assertThat(result.getModel()).isEqualTo("A4");
        assertThat(result.getType()).isEqualTo(VehicleType.OBERKLASSE);
        assertThat(result.getLocation()).isEqualTo("München");
        assertThat(result.getDailyPrice()).isEqualTo(80.0);
        
        verify(vehicleRepository).save(testVehicle);
        verify(auditService).logAction(anyString(), eq("VEHICLE_UPDATED"), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Sollte Exception werfen wenn Fahrzeug nicht gefunden wird")
    void shouldThrowExceptionWhenVehicleNotFound() {
        // Given
        when(vehicleRepository.findById(1L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> vehicleManagementService.updateVehicle(
                1L, "Audi", "A4", null, null, null, "employee", "127.0.0.1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nicht gefunden");
    }

    @Test
    @DisplayName("Sollte Fahrzeug außer Betrieb setzen können")
    void shouldSetVehicleOutOfService() {
        // Given
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        vehicleManagementService.setVehicleOutOfService(1L, "employee", "127.0.0.1");

        // Then
        assertThat(testVehicle.getStatus()).isEqualTo(VehicleStatus.AUSSER_BETRIEB);
        verify(vehicleRepository).save(testVehicle);
        verify(auditService).logAction(anyString(), eq("VEHICLE_OUT_OF_SERVICE"), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Sollte alle Fahrzeuge abrufen können")
    void shouldGetAllVehicles() {
        // Given
        Vehicle vehicle2 = Vehicle.builder()
                .licensePlate(LicensePlate.of("M-CD 5678"))
                .brand("Mercedes")
                .model("C220")
                .type(VehicleType.MITTELKLASSE)
                .mileage(30000L)
                .location("München")
                .status(VehicleStatus.VERFÜGBAR)
                .dailyPrice(65.0)
                .build();
        
        when(vehicleRepository.findAll()).thenReturn(List.of(testVehicle, vehicle2));

        // When
        List<Vehicle> result = vehicleManagementService.getAllVehicles();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(testVehicle, vehicle2);
    }

    @Test
    @DisplayName("Sollte Fahrzeug nach ID abrufen können")
    void shouldGetVehicleById() {
        // Given
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));

        // When
        Vehicle result = vehicleManagementService.getVehicleById(1L);

        // Then
        assertThat(result).isEqualTo(testVehicle);
    }

    @Test
    @DisplayName("Sollte Exception werfen wenn Fahrzeug-ID nicht gefunden wird")
    void shouldThrowExceptionWhenVehicleIdNotFound() {
        // Given
        when(vehicleRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> vehicleManagementService.getVehicleById(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nicht gefunden");
    }

    @Test
    @DisplayName("Sollte Fahrzeug aktualisieren können mit teilweisen Daten")
    void shouldUpdateVehicleWithPartialData() {
        // Given
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When - nur brand ändern
        Vehicle result = vehicleManagementService.updateVehicle(
                1L, "Audi", null, null, null, null, "employee", "127.0.0.1");

        // Then
        assertThat(result.getBrand()).isEqualTo("Audi");
        assertThat(result.getModel()).isEqualTo("320d"); // Unverändert
        verify(vehicleRepository).save(testVehicle);
    }

    @Test
    @DisplayName("Sollte Exception werfen wenn Fahrzeug nicht gefunden wird beim Außer-Betrieb-Setzen")
    void shouldThrowExceptionWhenVehicleNotFoundOnOutOfService() {
        // Given
        when(vehicleRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> vehicleManagementService.setVehicleOutOfService(999L, "employee", "127.0.0.1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nicht gefunden");
    }
}

