package com.erp.montfortuganda.student.entity;

import com.erp.montfortuganda.admission.entity.ErpApplication;
import com.erp.montfortuganda.school.entity.Branch;
import com.erp.montfortuganda.school.entity.ErpAcademicYear;
import com.erp.montfortuganda.school.entity.ErpALevelCombination;
import com.erp.montfortuganda.school.entity.SchoolClass;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDateTime;

@Data
@Entity
@DynamicUpdate
@Table(
        name = "erp_student_a_level_selections",
        indexes = {
                @Index(name = "idx_a_level_selection_application",
                        columnList = "application_id"),
                @Index(name = "idx_a_level_selection_student",
                        columnList = "student_id"),
                @Index(name = "idx_a_level_selection_enrollment",
                        columnList = "enrollment_id"),
                @Index(name = "idx_a_level_selection_branch_year_class",
                        columnList = "branch_id, academic_year_id, class_id"),
                @Index(name = "idx_a_level_selection_combination",
                        columnList = "combination_id"),
                @Index(name = "idx_a_level_selection_status",
                        columnList = "status")
        }
)
@EqualsAndHashCode(exclude = {
        "application", "student", "enrollment", "branch",
        "academicYear", "schoolClass", "combination"
})
@ToString(exclude = {
        "application", "student", "enrollment", "branch",
        "academicYear", "schoolClass", "combination"
})
public class ErpStudentALevelSelection {

    public enum SelectionStatus {
        PENDING,
        CONFIRMED,
        SUPERSEDED,
        CANCELLED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "selection_id")
    private Long selectionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "application_id",
            foreignKey = @ForeignKey(
                    name = "fk_a_level_selection_application"
            )
    )
    private ErpApplication application;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "student_id",
            foreignKey = @ForeignKey(
                    name = "fk_a_level_selection_student"
            )
    )
    private ErpStudent student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "enrollment_id",
            foreignKey = @ForeignKey(
                    name = "fk_a_level_selection_enrollment"
            )
    )
    private ErpStudentEnrollment enrollment;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "branch_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_a_level_selection_branch"
            )
    )
    private Branch branch;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "academic_year_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_a_level_selection_year"
            )
    )
    private ErpAcademicYear academicYear;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "class_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_a_level_selection_class"
            )
    )
    private SchoolClass schoolClass;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "combination_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_a_level_selection_combination"
            )
    )
    private ErpALevelCombination combination;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SelectionStatus status = SelectionStatus.PENDING;

    @Column(name = "selected_at")
    private LocalDateTime selectedAt;

    @Column(name = "selected_by")
    private Long selectedBy;

    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Database-generated column.
     * Read-only in Hibernate.
     */
    @Column(
            name = "current_application_id",
            insertable = false,
            updatable = false
    )
    private Long currentApplicationId;

    /**
     * Database-generated column.
     * Read-only in Hibernate.
     */
    @Column(
            name = "current_student_id",
            insertable = false,
            updatable = false
    )
    private Long currentStudentId;

    @PrePersist
    private void onCreate() {
        if (status == null) {
            status = SelectionStatus.PENDING;
        }

        if (version == null) {
            version = 0L;
        }

        LocalDateTime now = LocalDateTime.now();

        if (createdAt == null) {
            createdAt = now;
        }

        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    @PreUpdate
    private void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}