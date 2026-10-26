package com.erp.montfortuganda.fees.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * One configurable charge inside a fee structure. Fee categories are stored
 * as values, not a hardcoded enum, so a branch can add new fee patterns.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@DynamicUpdate
@Table(
        name = "erp_fee_structure_items",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_fee_structure_item_name",
                columnNames = {"fee_structure_id", "fee_type", "fee_name"}
        ),
        indexes = {
                @Index(name = "idx_fee_structure_item_type", columnList = "fee_type"),
                @Index(name = "idx_fee_structure_item_active", columnList = "fee_structure_id, active, display_order")
        }
)
public class ErpFeeStructureItem implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public enum ChargingFrequency {
        PER_TERM,
        PER_ACADEMIC_YEAR,
        ONE_TIME,
        PER_ADMISSION,
        AS_REQUIRED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fee_structure_item_id")
    private Long feeStructureItemId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "fee_structure_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_fee_structure_item_structure")
    )
    private ErpFeeStructure feeStructure;

    @NotBlank
    @Size(max = 50)
    @Column(name = "fee_type", nullable = false, length = 50)
    private String feeType;

    @NotBlank
    @Size(max = 150)
    @Column(name = "fee_name", nullable = false, length = 150)
    private String feeName;

    @NotNull
    @DecimalMin(value = "0.00")
    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "charging_frequency", nullable = false, length = 30)
    private ChargingFrequency chargingFrequency = ChargingFrequency.PER_TERM;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @NotNull
    @Column(name = "mandatory", nullable = false)
    private Boolean mandatory = true;

    @NotNull
    @Min(0)
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;

    @NotNull
    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Size(max = 500)
    @Column(name = "remarks", length = 500)
    private String remarks;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    @Column(name = "row_version", nullable = false)
    private Long rowVersion = 0L;

    @PrePersist
    private void beforeInsert() {
        normalize();
        if (chargingFrequency == null) chargingFrequency = ChargingFrequency.PER_TERM;
        if (mandatory == null) mandatory = true;
        if (displayOrder == null) displayOrder = 0;
        if (active == null) active = true;
        if (rowVersion == null) rowVersion = 0L;
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
    }

    @PreUpdate
    private void beforeUpdate() {
        normalize();
        updatedAt = LocalDateTime.now();
    }

    private void normalize() {
        if (feeType != null) feeType = feeType.trim().toUpperCase(java.util.Locale.ROOT);
        if (feeName != null) feeName = feeName.trim();
        if (remarks != null) remarks = remarks.trim();
    }
}
