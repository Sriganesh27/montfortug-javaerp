package com.erp.montfortuganda.student.entity;

import com.erp.montfortuganda.school.entity.Branch;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.Immutable;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Immutable historical record of a student's past enrollments.
 */
@Data
@Entity
@Immutable
@Table(name = "erp_student_enrollment_history",
        indexes = {
                @Index(name = "idx_hist_student", columnList = "student_id"),
                @Index(name = "idx_hist_enrollment", columnList = "enrollment_id"),
                @Index(name = "idx_hist_branch", columnList = "branch_id"),
                @Index(name = "idx_hist_admission", columnList = "admission_no"),
                @Index(name = "idx_hist_year", columnList = "academic_year_id"),
                @Index(name = "idx_hist_class", columnList = "class_id"),
                @Index(name = "idx_hist_status", columnList = "enrollment_status"),
                @Index(name = "idx_hist_effective_date", columnList = "effective_date"),
                @Index(name = "idx_hist_branch_year", columnList = "branch_id, academic_year_id")
        }
)
@EqualsAndHashCode(exclude = {"student", "enrollment", "branch"})
@ToString(exclude = {"student", "enrollment", "branch"})
public class ErpStudentEnrollmentHistory {

    // ==========================================
    // PRIMARY KEY
    // ==========================================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "enrollment_history_id") // <-- Updated to match your new SQL
    private Long enrollmentHistoryId;        // <-- Updated Java field name

    // ==========================================
    // RELATIONSHIPS
    // ==========================================
    @NotNull(message = "Student is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private ErpStudent student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrollment_id")
    private ErpStudentEnrollment enrollment;

    @NotNull(message = "Branch is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    // ==========================================
    // BUSINESS IDENTIFIERS
    // ==========================================
    @NotNull(message = "Admission number is required")
    @Size(max = 50, message = "Admission number cannot exceed 50 characters")
    @Column(name = "admission_no", nullable = false, length = 50)
    private String admissionNo;

    // ==========================================
    // ACADEMIC STRUCTURE
    // ==========================================
    @NotNull(message = "Academic year is required")
    @Column(name = "academic_year_id", nullable = false)
    private Long academicYearId;

    @NotNull(message = "Class is required")
    @Column(name = "class_id", nullable = false)
    private Long classId;

    @Column(name = "section_id")
    private Long sectionId;

    @Column(name = "stream_id")
    private Long streamId;

    @Column(name = "house_id")
    private Long houseId;

    @Column(name = "hostel_id")
    private Long hostelId;

    @Column(name = "bed_id")
    private Long bedId;

    @Size(max = 20, message = "Roll number cannot exceed 20 characters")
    @Column(name = "roll_no", length = 20)
    private String rollNo;

    // ==========================================
    // STATUS FIELDS (Decoupled from Enums for safe history)
    // ==========================================
    @NotNull(message = "Admission type is required")
    @Size(max = 20, message = "Admission type cannot exceed 20 characters")
    @Column(name = "admission_type", nullable = false, length = 20)
    private String admissionType;

    @NotNull(message = "Promotion type is required")
    @Size(max = 20, message = "Promotion type cannot exceed 20 characters")
    @Column(name = "promotion_type", nullable = false, length = 20)
    private String promotionType;

    @NotNull(message = "Enrollment status is required")
    @Size(max = 20, message = "Enrollment status cannot exceed 20 characters")
    @Column(name = "enrollment_status", nullable = false, length = 20)
    private String enrollmentStatus;

    // ==========================================
    // TIMELINE FIELDS
    // ==========================================
    @NotNull(message = "Joining date is required")
    @Column(name = "joining_date", nullable = false)
    private LocalDate joiningDate;

    @Column(name = "leaving_date")
    private LocalDate leavingDate;

    @NotNull(message = "Effective date is required")
    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;

    // ==========================================
    // CHANGE CONTEXT & APPROVAL
    // ==========================================
    @Size(max = 255, message = "Change reason cannot exceed 255 characters")
    @Column(name = "change_reason", length = 255)
    private String changeReason;

    @Column(name = "approved_by")
    private Long approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Lob
    @Column(name = "remarks")
    private String remarks;

    // ==========================================
    // AUDIT
    // ==========================================
    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // ==========================================
    // JPA LIFECYCLE CALLBACKS
    // ==========================================
    @PrePersist
    protected void onCreate() {
        if (this.effectiveDate == null) {
            this.effectiveDate = LocalDate.now();
        }
        this.createdAt = LocalDateTime.now();
    }
}