package com.erp.montfortuganda.school;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@DynamicUpdate
@Table(
        name = "erp_academic_terms",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_year_term_code",
                        columnNames = {"academic_year_id", "term_code"}
                )
        }
)
@EqualsAndHashCode(exclude = "academicYear")
@ToString(exclude = "academicYear")
public class ErpAcademicTerm implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public enum Status {
        PLANNED,
        ACTIVE,
        CLOSED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "term_id")
    private Long termId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "academic_year_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_term_year")
    )
    private ErpAcademicYear academicYear;

    @NotBlank
    @Size(max = 20)
    @Column(name = "term_code", nullable = false, length = 20)
    private String termCode;

    @NotBlank
    @Size(max = 100)
    @Column(name = "term_name", nullable = false, length = 100)
    private String termName;

    @NotNull
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @NotNull
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @NotNull
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private Status status = Status.PLANNED;

    @Column(name = "current_term", nullable = false)
    private Boolean currentTerm = false;

    @Size(max = 500)
    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    private void onCreate() {

        validateDates();

        if (active == null)
            active = true;

        if (currentTerm == null)
            currentTerm = false;

        if (status == null)
            status = Status.PLANNED;

        LocalDateTime now = LocalDateTime.now();

        if (createdAt == null)
            createdAt = now;

        if (updatedAt == null)
            updatedAt = now;
    }

    @PreUpdate
    private void onUpdate() {

        validateDates();

        updatedAt = LocalDateTime.now();
    }

    private void validateDates() {

        if (startDate != null &&
                endDate != null &&
                endDate.isBefore(startDate)) {

            throw new IllegalStateException(
                    "Term end date cannot be before start date.");
        }
    }

}