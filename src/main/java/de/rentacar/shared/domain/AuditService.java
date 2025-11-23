package de.rentacar.shared.domain;

import de.rentacar.shared.infrastructure.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Domain Service für Audit-Logging (NFR5)
 */
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    @Transactional
    public void logAction(String username, String action, String resourceType, String resourceId, String details, String ipAddress) {
        AuditLog auditLog = AuditLog.builder()
                .username(username)
                .action(action)
                .resourceType(resourceType)
                .resourceId(resourceId)
                .details(details)
                .ipAddress(ipAddress)
                .build();
        auditLogRepository.save(auditLog);
    }
}

