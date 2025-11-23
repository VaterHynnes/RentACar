package de.rentacar.rental.domain;

import de.rentacar.shared.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Entity für Schadensberichte
 */
@Entity
@Table(name = "damage_reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DamageReport extends BaseEntity {

    @Column(nullable = false)
    private Long rentalId;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private java.math.BigDecimal repairCost;

    @Column(length = 1000)
    private String notes;
}

