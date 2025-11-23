package de.rentacar.customer.application;

import de.rentacar.customer.domain.Customer;
import de.rentacar.customer.domain.CustomerRepository;
import de.rentacar.customer.domain.EncryptedString;
import de.rentacar.customer.infrastructure.EncryptionService;
import de.rentacar.shared.domain.AuditService;
import de.rentacar.shared.security.Role;
import de.rentacar.shared.security.User;
import de.rentacar.shared.security.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * Application Service für Kundenverwaltung (Use Cases)
 */
@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final EncryptionService encryptionService;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    /**
     * Use Case: Kunde registrieren
     */
    @Transactional
    public Customer registerCustomer(String username, String password, String firstName, 
                                    String lastName, String email, String phone, 
                                    String address, String driverLicenseNumber,
                                    String ipAddress) {
        // Prüfe ob Benutzername bereits existiert
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Benutzername bereits vergeben");
        }

        // Erstelle User für Spring Security
        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .roles(Set.of(Role.ROLE_CUSTOMER))
                .enabled(true)
                .build();
        userRepository.save(user);

        // Verschlüssele sensible Daten (DSGVO-konform)
        EncryptedString encryptedEmail = EncryptedString.of(encryptionService.encrypt(email));
        EncryptedString encryptedPhone = EncryptedString.of(encryptionService.encrypt(phone));
        EncryptedString encryptedAddress = EncryptedString.of(encryptionService.encrypt(address));
        EncryptedString encryptedLicense = EncryptedString.of(encryptionService.encrypt(driverLicenseNumber));

        Customer customer = Customer.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .firstName(firstName)
                .lastName(lastName)
                .email(encryptedEmail)
                .phone(encryptedPhone)
                .address(encryptedAddress)
                .driverLicenseNumber(encryptedLicense)
                .build();

        Customer savedCustomer = customerRepository.save(customer);

        auditService.logAction(username, "CUSTOMER_REGISTERED", "Customer", 
                savedCustomer.getId().toString(), 
                "Kunde registriert",
                ipAddress);

        return savedCustomer;
    }

    /**
     * Use Case: Kundendaten aktualisieren
     */
    @Transactional
    public Customer updateCustomerData(Long customerId, String firstName, String lastName, 
                                      String email, String phone, String address,
                                      String username, String ipAddress) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Kunde nicht gefunden"));

        // Verschlüssele neue Daten
        EncryptedString encryptedEmail = email != null ? 
                EncryptedString.of(encryptionService.encrypt(email)) : null;
        EncryptedString encryptedPhone = phone != null ? 
                EncryptedString.of(encryptionService.encrypt(phone)) : null;
        EncryptedString encryptedAddress = address != null ? 
                EncryptedString.of(encryptionService.encrypt(address)) : null;

        customer.updatePersonalData(firstName, lastName, encryptedEmail, encryptedPhone, encryptedAddress);
        
        Customer savedCustomer = customerRepository.save(customer);

        auditService.logAction(username, "CUSTOMER_UPDATED", "Customer", 
                customerId.toString(), 
                "Kundendaten aktualisiert",
                ipAddress);

        return savedCustomer;
    }

    /**
     * Use Case: Kunde nach ID abrufen
     */
    @Transactional(readOnly = true)
    public Customer getCustomerById(Long customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Kunde nicht gefunden"));
    }

    /**
     * Use Case: Kunde nach Benutzername abrufen
     */
    @Transactional(readOnly = true)
    public Customer getCustomerByUsername(String username) {
        return customerRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Kunde nicht gefunden"));
    }
}

