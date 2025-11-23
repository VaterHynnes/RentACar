package de.rentacar.vehicle.web;

import de.rentacar.vehicle.application.VehicleManagementService;
import de.rentacar.vehicle.domain.Vehicle;
import de.rentacar.vehicle.domain.VehicleType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * REST Controller für Fahrzeugverwaltung
 */
@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleManagementService vehicleManagementService;

    @PostMapping
    public ResponseEntity<Vehicle> addVehicle(@RequestBody CreateVehicleRequest request,
                                             Authentication authentication,
                                             HttpServletRequest httpRequest) {
        Vehicle vehicle = vehicleManagementService.addVehicle(
                request.licensePlate(),
                request.brand(),
                request.model(),
                request.type(),
                request.mileage(),
                request.location(),
                request.dailyPrice(),
                authentication.getName(),
                httpRequest.getRemoteAddr()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(vehicle);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Vehicle> updateVehicle(@PathVariable Long id,
                                                @RequestBody UpdateVehicleRequest request,
                                                Authentication authentication,
                                                HttpServletRequest httpRequest) {
        Vehicle vehicle = vehicleManagementService.updateVehicle(
                id,
                request.brand(),
                request.model(),
                request.type(),
                request.location(),
                request.dailyPrice(),
                authentication.getName(),
                httpRequest.getRemoteAddr()
        );
        return ResponseEntity.ok(vehicle);
    }

    @PutMapping("/{id}/out-of-service")
    public ResponseEntity<Void> setOutOfService(@PathVariable Long id,
                                               Authentication authentication,
                                               HttpServletRequest httpRequest) {
        vehicleManagementService.setVehicleOutOfService(
                id,
                authentication.getName(),
                httpRequest.getRemoteAddr()
        );
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<Vehicle>> getAllVehicles() {
        return ResponseEntity.ok(vehicleManagementService.getAllVehicles());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Vehicle> getVehicleById(@PathVariable Long id) {
        return ResponseEntity.ok(vehicleManagementService.getVehicleById(id));
    }

    public record CreateVehicleRequest(
            String licensePlate,
            String brand,
            String model,
            VehicleType type,
            Long mileage,
            String location,
            Double dailyPrice
    ) {}

    public record UpdateVehicleRequest(
            String brand,
            String model,
            VehicleType type,
            String location,
            Double dailyPrice
    ) {}
}

