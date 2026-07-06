package com.erp.montfortuganda.school;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@DynamicUpdate
@Table(name = "erp_academic_years")
public class ErpAcademicYear implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public enum Status { PLANNED, ACTIVE, CLOSED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "academic_year_id")
    private Long academicYearId;

    @NotBlank
    @Size(max = 20)
    @Column(name = "academic_year_code", nullable = false, unique = true, length = 20)
    private String academicYearCode;

    @NotBlank
    @Size(max = 100)
    @Column(name = "academic_year_name", nullable = false, length = 100)
    private String academicYearName;

    @NotNull
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @NotNull
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "admission_start_date")
    private LocalDate admissionStartDate;

    @Column(name = "admission_end_date")
    private LocalDate admissionEndDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private Status status = Status.PLANNED;

    @NotNull
    @Column(name = "current_year", nullable = false)
    private Boolean currentYear = false;

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

        if (active == null) {
            active = true;
        }
        if (currentYear == null) {
            currentYear = false;
        }
        if (status == null) {
            status = Status.PLANNED;
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
        validateDates();
        updatedAt = LocalDateTime.now();
    }

    private void validateDates() {
        if (endDate != null && startDate != null && endDate.isBefore(startDate)) {
            throw new IllegalStateException("Academic Year end date cannot be before start date.");
        }

        if (admissionStartDate != null && admissionEndDate != null && admissionEndDate.isBefore(admissionStartDate)) {
            throw new IllegalStateException("Admission end date cannot be before admission start date.");
        }
    }
    // ==========================================
    // RELATIONSHIPS
    // ==========================================

    @OneToMany(mappedBy = "academicYear", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<ErpSection> sections = new java.util.ArrayList<>();

    public void addSection(ErpSection section) {
        sections.add(section);
        section.setAcademicYear(this);
    }

}