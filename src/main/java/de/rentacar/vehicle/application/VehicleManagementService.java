package de.rentacar.vehicle.application;

import de.rentacar.shared.domain.AuditService;
import de.rentacar.vehicle.domain.LicensePlate;
import de.rentacar.vehicle.domain.Vehicle;
import de.rentacar.vehicle.domain.VehicleRepository;
import de.rentacar.vehicle.domain.VehicleStatus;
import de.rentacar.vehicle.domain.VehicleType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Application Service für Fahrzeugverwaltung (Use Cases)
 */
@Service
@RequiredArgsConstructor
public class VehicleManagementService {

    private final VehicleRepository vehicleRepository;
    private final AuditService auditService;

    /**
     * Use Case: Fahrzeug hinzufügen (Mitarbeiter)
     */
    @Transactional
    public Vehicle addVehicle(String licensePlate, String brand, String model, 
                             VehicleType type, Long mileage, String location, 
                             Double dailyPrice, String username, String ipAddress) {
        LicensePlate plate = LicensePlate.of(licensePlate);
        
        // Prüfe ob Kennzeichen bereits existiert
        vehicleRepository.findByLicensePlate(plate)
                .ifPresent(v -> {
                    throw new IllegalArgumentException("Fahrzeug mit diesem Kennzeichen existiert bereits");
                });

        Vehicle vehicle = Vehicle.builder()
                .licensePlate(plate)
                .brand(brand)
                .model(model)
                .type(type)
                .mileage(mileage)
                .location(location)
                .dailyPrice(dailyPrice)
                .status(VehicleStatus.VERFÜGBAR)
                .build();

        Vehicle savedVehicle = vehicleRepository.save(vehicle);

        auditService.logAction(username, "VEHICLE_ADDED", "Vehicle", 
                savedVehicle.getId().toString(), 
                String.format("Fahrzeug hinzugefügt: %s", licensePlate),
                ipAddress);

        return savedVehicle;
    }

    /**
     * Use Case: Fahrzeug bearbeiten (Mitarbeiter)
     */
    @Transactional
    public Vehicle updateVehicle(Long vehicleId, String brand, String model, 
                                VehicleType type, String location, Double dailyPrice,
                                String username, String ipAddress) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Fahrzeug nicht gefunden"));

        if (brand != null) vehicle.setBrand(brand);
        if (model != null) vehicle.setModel(model);
        if (type != null) vehicle.setType(type);
        if (location != null) vehicle.setLocation(location);
        if (dailyPrice != null) vehicle.setDailyPrice(dailyPrice);

        Vehicle savedVehicle = vehicleRepository.save(vehicle);

        auditService.logAction(username, "VEHICLE_UPDATED", "Vehicle", 
                vehicleId.toString(), 
                String.format("Fahrzeug aktualisiert: %s", vehicle.getLicensePlate()),
                ipAddress);

        return savedVehicle;
    }

    /**
     * Use Case: Fahrzeug außer Betrieb setzen (Mitarbeiter)
     */
    @Transactional
    public void setVehicleOutOfService(Long vehicleId, String username, String ipAddress) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Fahrzeug nicht gefunden"));

        vehicle.markAsOutOfService();
        vehicleRepository.save(vehicle);

        auditService.logAction(username, "VEHICLE_OUT_OF_SERVICE", "Vehicle", 
                vehicleId.toString(), 
                String.format("Fahrzeug außer Betrieb gesetzt: %s", vehicle.getLicensePlate()),
                ipAddress);
    }

    /**
     * Use Case: Alle Fahrzeuge abrufen
     */
    @Transactional(readOnly = true)
    public List<Vehicle> getAllVehicles() {
        return vehicleRepository.findAll();
    }

    /**
     * Use Case: Fahrzeug nach ID abrufen
     */
    @Transactional(readOnly = true)
    public Vehicle getVehicleById(Long vehicleId) {
        return vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Fahrzeug nicht gefunden"));
    }
}

