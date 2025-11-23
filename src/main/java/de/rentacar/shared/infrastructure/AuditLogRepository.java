package de.rentacar.shared.infrastructure;

import de.rentacar.shared.domain.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByUsername(String username);
    List<AuditLog> findByResourceTypeAndResourceId(String resourceType, String resourceId);
    List<AuditLog> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
}

