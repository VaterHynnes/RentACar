package de.rentacar.vehicle.infrastructure;

import de.rentacar.vehicle.domain.LicensePlate;
import de.rentacar.vehicle.domain.Vehicle;
import de.rentacar.vehicle.domain.VehicleRepository;
import de.rentacar.vehicle.domain.VehicleStatus;
import de.rentacar.vehicle.domain.VehicleType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository Implementation (Infrastructure Layer)
 */
@Repository
@RequiredArgsConstructor
public class VehicleRepositoryImpl implements VehicleRepository {

    private final VehicleJpaRepository jpaRepository;

    @Override
    public Vehicle save(Vehicle vehicle) {
        return jpaRepository.save(vehicle);
    }

    @Override
    public Optional<Vehicle> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<Vehicle> findByLicensePlate(LicensePlate licensePlate) {
        return jpaRepository.findByLicensePlateValue(licensePlate.getValue());
    }

    @Override
    public List<Vehicle> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public List<Vehicle> findByType(VehicleType type) {
        return jpaRepository.findByType(type);
    }

    @Override
    public List<Vehicle> findByStatus(VehicleStatus status) {
        return jpaRepository.findByStatus(status);
    }

    @Override
    public List<Vehicle> findByLocation(String location) {
        return jpaRepository.findByLocation(location);
    }

    @Override
    public List<Vehicle> findAvailableVehicles(VehicleType type, String location, LocalDate startDate, LocalDate endDate) {
        return jpaRepository.findAvailableVehicles(type, location, startDate, endDate);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
}

