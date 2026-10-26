package com.erp.montfortuganda.fees.entity;

import com.erp.montfortuganda.school.entity.Branch;
import com.erp.montfortuganda.school.entity.ErpAcademicTerm;
import com.erp.montfortuganda.school.entity.ErpAcademicYear;
import com.erp.montfortuganda.school.entity.SchoolClass;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * A versioned fee structure for one branch, class and academic term.
 * The table already exists in the production schema; this entity maps it
 * without requiring a new table or changing the existing schema.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@DynamicUpdate
@Table(
        name = "erp_fee_structures",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_fee_structure_class_term_version",
                columnNames = {"class_id", "term_id", "version_no"}
        ),
        indexes = {
                @Index(name = "idx_fee_structure_branch_year", columnList = "branch_id, academic_year_id"),
                @Index(name = "idx_fee_structure_term_status", columnList = "term_id, structure_status"),
                @Index(name = "idx_fee_structure_class_status", columnList = "class_id, structure_status")
        }
)
public class ErpFeeStructure implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public enum StructureStatus {
        DRAFT,
        PUBLISHED,
        ARCHIVED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fee_structure_id")
    private Long feeStructureId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "branch_id", nullable = false, foreignKey = @ForeignKey(name = "fk_fee_structure_branch"))
    private Branch branch;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "academic_year_id", nullable = false, foreignKey = @ForeignKey(name = "fk_fee_structure_year"))
    private ErpAcademicYear academicYear;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "class_id", nullable = false, foreignKey = @ForeignKey(name = "fk_fee_structure_class"))
    private SchoolClass schoolClass;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "term_id", nullable = false, foreignKey = @ForeignKey(name = "fk_fee_structure_term"))
    private ErpAcademicTerm term;

    @NotNull
    @Positive
    @Column(name = "version_no", nullable = false)
    private Integer versionNo = 1;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "structure_status", nullable = false, length = 20)
    private StructureStatus structureStatus = StructureStatus.DRAFT;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

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
        if (versionNo == null) versionNo = 1;
        if (structureStatus == null) structureStatus = StructureStatus.DRAFT;
        if (rowVersion == null) rowVersion = 0L;
        if (remarks != null) remarks = remarks.trim();
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
    }

    @PreUpdate
    private void beforeUpdate() {
        if (remarks != null) remarks = remarks.trim();
        updatedAt = LocalDateTime.now();
    }
}
