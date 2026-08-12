package com.erp.montfortuganda.admission.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Data
@Entity
@DynamicUpdate
@Table(name = "erp_application_interview_marks")
@EqualsAndHashCode(exclude = "interview")
@ToString(exclude = "interview")
public class ErpApplicationInterviewMark implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "interview_mark_id")
    private Long interviewMarkId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "interview_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_interview_marks_interview"
            )
    )
    private ErpApplicationInterview interview;

    /**
     * References erp_subjects.subject_id.
     *
     * Stored as an ID here so Admission does not create a duplicate Subject
     * entity model. Subject existence/active status is validated by service
     * logic against the existing school subject master.
     */
    @NotNull
    @Column(name = "subject_id", nullable = false)
    private Long subjectId;

    @NotNull
    @DecimalMin(value = "0.01")
    @Column(
            name = "maximum_marks",
            nullable = false,
            precision = 7,
            scale = 2
    )
    private BigDecimal maximumMarks;

    @NotNull
    @DecimalMin(value = "0.00")
    @Column(
            name = "obtained_marks",
            nullable = false,
            precision = 7,
            scale = 2
    )
    private BigDecimal obtainedMarks;

    @DecimalMin(value = "0.00")
    @DecimalMax(value = "100.00")
    @Column(
            name = "percentage",
            precision = 6,
            scale = 2
    )
    private BigDecimal percentage;

    @Size(max = 500)
    @Column(name = "remarks", length = 500)
    private String remarks;

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

    @Column(
            name = "updated_at",
            nullable = false
    )
    private LocalDateTime updatedAt;

    @Column(
            name = "active",
            nullable = false
    )
    private Boolean active = true;

    @Version
    @Column(
            name = "version",
            nullable = false
    )
    private Long version;

    @PrePersist
    private void onCreate() {
        validateAndCalculatePercentage();

        if (active == null) {
            active = true;
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
        validateAndCalculatePercentage();
        updatedAt = LocalDateTime.now();
    }

    private void validateAndCalculatePercentage() {
        if (maximumMarks == null || obtainedMarks == null) {
            percentage = null;
            return;
        }

        if (maximumMarks.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException(
                    "Maximum marks must be greater than zero."
            );
        }

        if (obtainedMarks.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException(
                    "Obtained marks cannot be negative."
            );
        }

        if (obtainedMarks.compareTo(maximumMarks) > 0) {
            throw new IllegalStateException(
                    "Obtained marks cannot exceed maximum marks."
            );
        }

        percentage =
                obtainedMarks
                        .multiply(BigDecimal.valueOf(100))
                        .divide(
                                maximumMarks,
                                2,
                                RoundingMode.HALF_UP
                        );
    }
}
