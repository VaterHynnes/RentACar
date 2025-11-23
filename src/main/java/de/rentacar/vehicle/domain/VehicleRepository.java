package de.rentacar.vehicle.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository Interface für Vehicle Aggregate (Domain Layer)
 */
public interface VehicleRepository {
    Vehicle save(Vehicle vehicle);
    Optional<Vehicle> findById(Long id);
    Optional<Vehicle> findByLicensePlate(LicensePlate licensePlate);
    List<Vehicle> findAll();
    List<Vehicle> findByType(VehicleType type);
    List<Vehicle> findByStatus(VehicleStatus status);
    List<Vehicle> findByLocation(String location);
    List<Vehicle> findAvailableVehicles(VehicleType type, String location, LocalDate startDate, LocalDate endDate);
    void deleteById(Long id);
}

