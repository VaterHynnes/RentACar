package de.rentacar.shared.domain;

import de.rentacar.shared.infrastructure.AuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

/**
 * Unit-Tests für AuditService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuditService Tests")
class AuditServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditService auditService;

    @Test
    @DisplayName("Sollte Audit-Log-Eintrag erstellen")
    void shouldLogAction() {
        // Given
        String username = "testuser";
        String action = "TEST_ACTION";
        String resourceType = "TestResource";
        String resourceId = "123";
        String details = "Test details";
        String ipAddress = "127.0.0.1";

        // When
        auditService.logAction(username, action, resourceType, resourceId, details, ipAddress);

        // Then
        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());
        
        AuditLog savedLog = captor.getValue();
        assertThat(savedLog.getUsername()).isEqualTo(username);
        assertThat(savedLog.getAction()).isEqualTo(action);
        assertThat(savedLog.getResourceType()).isEqualTo(resourceType);
        assertThat(savedLog.getResourceId()).isEqualTo(resourceId);
        assertThat(savedLog.getDetails()).isEqualTo(details);
        assertThat(savedLog.getIpAddress()).isEqualTo(ipAddress);
        // Timestamp wird in @PrePersist gesetzt, kann hier null sein
        // assertThat(savedLog.getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("Sollte Audit-Log mit null resourceId erstellen können")
    void shouldLogActionWithNullResourceId() {
        // When
        auditService.logAction("user", "ACTION", "Resource", null, "Details", "127.0.0.1");

        // Then
        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());
        
        assertThat(captor.getValue().getResourceId()).isNull();
    }
}

