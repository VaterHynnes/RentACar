package de.rentacar.customer.application;

import de.rentacar.customer.domain.Customer;
import de.rentacar.customer.domain.CustomerRepository;
import de.rentacar.customer.domain.EncryptedString;
import de.rentacar.customer.infrastructure.EncryptionService;
import de.rentacar.shared.domain.AuditService;
import de.rentacar.shared.security.Role;
import de.rentacar.shared.security.User;
import de.rentacar.shared.security.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit-Tests für CustomerService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CustomerService Tests")
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EncryptionService encryptionService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private CustomerService customerService;

    private Customer testCustomer;

    @BeforeEach
    void setUp() {
        testCustomer = Customer.builder()
                .username("testuser")
                .password("hashed-password")
                .firstName("Max")
                .lastName("Mustermann")
                .email(EncryptedString.of("encrypted-email"))
                .phone(EncryptedString.of("encrypted-phone"))
                .address(EncryptedString.of("encrypted-address"))
                .driverLicenseNumber(EncryptedString.of("encrypted-license"))
                .build();
        testCustomer.setId(1L);
    }

    @Test
    @DisplayName("Sollte Kunde registrieren können")
    void shouldRegisterCustomer() {
        // Given
        Customer savedCustomer = Customer.builder()
                .username("newuser")
                .firstName("John")
                .lastName("Doe")
                .email(EncryptedString.of("encrypted-value"))
                .phone(EncryptedString.of("encrypted-value"))
                .address(EncryptedString.of("encrypted-value"))
                .driverLicenseNumber(EncryptedString.of("encrypted-value"))
                .build();
        savedCustomer.setId(1L);
        
        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("hashed-password");
        when(encryptionService.encrypt(anyString())).thenReturn("encrypted-value");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(customerRepository.save(any(Customer.class))).thenReturn(savedCustomer);

        // When
        Customer result = customerService.registerCustomer(
                "newuser", "password123", "John", "Doe",
                "john@example.com", "0123456789", "Teststraße 1",
                "B123456", "127.0.0.1");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("newuser");
        assertThat(result.getFirstName()).isEqualTo("John");
        assertThat(result.getLastName()).isEqualTo("Doe");
        
        verify(userRepository).save(any(User.class));
        verify(customerRepository).save(any(Customer.class));
        verify(encryptionService, times(4)).encrypt(anyString());
        verify(auditService).logAction(anyString(), eq("CUSTOMER_REGISTERED"), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Sollte Exception werfen wenn Benutzername bereits existiert")
    void shouldThrowExceptionWhenUsernameExists() {
        // Given
        User existingUser = User.builder()
                .username("existinguser")
                .password("hashed")
                .roles(Set.of(Role.ROLE_CUSTOMER))
                .build();
        when(userRepository.findByUsername("existinguser")).thenReturn(Optional.of(existingUser));

        // When/Then
        assertThatThrownBy(() -> customerService.registerCustomer(
                "existinguser", "password", "John", "Doe",
                "john@example.com", "0123456789", "Teststraße 1",
                "B123456", "127.0.0.1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("bereits vergeben");

        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    @DisplayName("Sollte Kundendaten aktualisieren können")
    void shouldUpdateCustomerData() {
        // Given
        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(encryptionService.encrypt(anyString())).thenReturn("new-encrypted-value");
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Customer result = customerService.updateCustomerData(
                1L, "John", "Doe", "john@example.com",
                "0987654321", "Neue Straße 2", "testuser", "127.0.0.1");

        // Then
        assertThat(result.getFirstName()).isEqualTo("John");
        assertThat(result.getLastName()).isEqualTo("Doe");
        
        verify(customerRepository).save(testCustomer);
        verify(encryptionService, times(3)).encrypt(anyString());
        verify(auditService).logAction(anyString(), eq("CUSTOMER_UPDATED"), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Sollte Exception werfen wenn Kunde nicht gefunden wird")
    void shouldThrowExceptionWhenCustomerNotFound() {
        // Given
        when(customerRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> customerService.updateCustomerData(
                999L, "John", "Doe", null, null, null, "testuser", "127.0.0.1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nicht gefunden");
    }

    @Test
    @DisplayName("Sollte Kunde nach ID abrufen können")
    void shouldGetCustomerById() {
        // Given
        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));

        // When
        Customer result = customerService.getCustomerById(1L);

        // Then
        assertThat(result).isEqualTo(testCustomer);
    }

    @Test
    @DisplayName("Sollte Kunde nach Benutzername abrufen können")
    void shouldGetCustomerByUsername() {
        // Given
        when(customerRepository.findByUsername("testuser")).thenReturn(Optional.of(testCustomer));

        // When
        Customer result = customerService.getCustomerByUsername("testuser");

        // Then
        assertThat(result).isEqualTo(testCustomer);
    }

    @Test
    @DisplayName("Sollte Exception werfen wenn Kunde nach Benutzername nicht gefunden wird")
    void shouldThrowExceptionWhenCustomerByUsernameNotFound() {
        // Given
        when(customerRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> customerService.getCustomerByUsername("unknown"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nicht gefunden");
    }

    @Test
    @DisplayName("Sollte Kundendaten aktualisieren können mit teilweisen Daten")
    void shouldUpdateCustomerDataWithPartialData() {
        // Given
        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When - nur firstName ändern
        Customer result = customerService.updateCustomerData(
                1L, "John", null, null, null, null, "testuser", "127.0.0.1");

        // Then
        assertThat(result.getFirstName()).isEqualTo("John");
        assertThat(result.getLastName()).isEqualTo("Mustermann"); // Unverändert
        verify(customerRepository).save(testCustomer);
    }

    @Test
    @DisplayName("Sollte Exception werfen wenn Kunde-ID nicht gefunden wird")
    void shouldThrowExceptionWhenCustomerIdNotFound() {
        // Given
        when(customerRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> customerService.getCustomerById(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nicht gefunden");
    }
}

