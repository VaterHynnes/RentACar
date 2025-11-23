package de.rentacar.shared.infrastructure;

import de.rentacar.shared.security.Role;
import de.rentacar.shared.security.User;
import de.rentacar.shared.security.UserRepository;
import de.rentacar.vehicle.domain.LicensePlate;
import de.rentacar.vehicle.domain.Vehicle;
import de.rentacar.vehicle.domain.VehicleRepository;
import de.rentacar.vehicle.domain.VehicleStatus;
import de.rentacar.vehicle.domain.VehicleType;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Initialisiert Testdaten beim Start der Anwendung
 */
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Test-User erstellen
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .roles(Set.of(Role.ROLE_ADMIN))
                    .enabled(true)
                    .build();
            userRepository.save(admin);
        }

        if (userRepository.findByUsername("employee").isEmpty()) {
            User employee = User.builder()
                    .username("employee")
                    .password(passwordEncoder.encode("employee123"))
                    .roles(Set.of(Role.ROLE_EMPLOYEE))
                    .enabled(true)
                    .build();
            userRepository.save(employee);
        }

        if (userRepository.findByUsername("customer").isEmpty()) {
            User customer = User.builder()
                    .username("customer")
                    .password(passwordEncoder.encode("customer123"))
                    .roles(Set.of(Role.ROLE_CUSTOMER))
                    .enabled(true)
                    .build();
            userRepository.save(customer);
        }

        // Test-Fahrzeuge erstellen
        if (vehicleRepository.findAll().isEmpty()) {
            vehicleRepository.save(Vehicle.builder()
                    .licensePlate(LicensePlate.of("B-AB 1234"))
                    .brand("BMW")
                    .model("320d")
                    .type(VehicleType.MITTELKLASSE)
                    .mileage(50000L)
                    .location("Berlin")
                    .status(VehicleStatus.VERFÜGBAR)
                    .dailyPrice(60.0)
                    .build());

            vehicleRepository.save(Vehicle.builder()
                    .licensePlate(LicensePlate.of("M-CD 5678"))
                    .brand("Mercedes")
                    .model("C220")
                    .type(VehicleType.MITTELKLASSE)
                    .mileage(30000L)
                    .location("München")
                    .status(VehicleStatus.VERFÜGBAR)
                    .dailyPrice(65.0)
                    .build());

            vehicleRepository.save(Vehicle.builder()
                    .licensePlate(LicensePlate.of("H-EF 9012"))
                    .brand("VW")
                    .model("Golf")
                    .type(VehicleType.KOMPAKTKLASSE)
                    .mileage(40000L)
                    .location("Hamburg")
                    .status(VehicleStatus.VERFÜGBAR)
                    .dailyPrice(40.0)
                    .build());

            vehicleRepository.save(Vehicle.builder()
                    .licensePlate(LicensePlate.of("B-GH 3456"))
                    .brand("Audi")
                    .model("Q5")
                    .type(VehicleType.SUV)
                    .mileage(60000L)
                    .location("Berlin")
                    .status(VehicleStatus.VERFÜGBAR)
                    .dailyPrice(80.0)
                    .build());
        }
    }
}

