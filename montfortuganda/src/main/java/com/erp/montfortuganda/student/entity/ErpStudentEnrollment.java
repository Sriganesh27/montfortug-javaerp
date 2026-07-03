package com.erp.montfortuganda.student.entity;

import com.erp.montfortuganda.school.Branch;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Current active enrollment of a student.
 * Historical enrollments are stored in erp_student_enrollment_history.
 */
@Data
@Entity
@DynamicUpdate
@Table(name = "erp_student_enrollment",
        indexes = {
                @Index(name = "idx_enrollment_student", columnList = "student_id"),
                @Index(name = "idx_enrollment_branch", columnList = "branch_id"),
                @Index(name = "idx_enrollment_admission", columnList = "admission_no"),
                @Index(name = "idx_enrollment_year", columnList = "academic_year_id"),
                @Index(name = "idx_enrollment_class", columnList = "class_id"),
                @Index(name = "idx_enrollment_status", columnList = "enrollment_status")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_student_current", columnNames = "student_id")
        }
)
@EqualsAndHashCode(exclude = {"student", "branch"})
@ToString(exclude = {"student", "branch"})
public class ErpStudentEnrollment {

    // ==========================================
    // NESTED ENUMS
    // ==========================================
    public enum AdmissionType {
        NEW, TRANSFER, READMISSION
    }

    public enum PromotionType {
        NEW, PROMOTED, RETAINED, TRANSFERRED
    }

    public enum EnrollmentStatus {
        ACTIVE, PROMOTED, TRANSFERRED, WITHDRAWN, GRADUATED, SUSPENDED, EXPELLED
    }

    // ==========================================
    // PRIMARY KEY
    // ==========================================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "enrollment_id")
    private Long enrollmentId;

    // ==========================================
    // RELATIONSHIPS
    // ==========================================
    @NotNull(message = "Student is required")
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private ErpStudent student;

    @NotNull(message = "Branch is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    // ==========================================
    // CORE FIELDS
    // ==========================================
    @NotNull(message = "Admission number is required")
    @Size(max = 50, message = "Admission number cannot exceed 50 characters")
    @Column(name = "admission_no", nullable = false, length = 50)
    private String admissionNo;

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

    @NotNull(message = "Admission type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "admission_type", nullable = false, length = 20)
    private AdmissionType admissionType = AdmissionType.NEW;

    @NotNull(message = "Promotion type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "promotion_type", nullable = false, length = 20)
    private PromotionType promotionType = PromotionType.NEW;

    @NotNull(message = "Enrollment status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "enrollment_status", nullable = false, length = 20)
    private EnrollmentStatus enrollmentStatus = EnrollmentStatus.ACTIVE;

    @NotNull(message = "Joining date is required")
    @Column(name = "joining_date", nullable = false)
    private LocalDate joiningDate;

    @Column(name = "leaving_date")
    private LocalDate leavingDate;

    @Column(name = "class_teacher_id")
    private Long classTeacherId;

    @Column(name = "fee_structure_id")
    private Long feeStructureId;

    @Column(name = "scholarship_id")
    private Long scholarshipId;

    @Column(name = "approved_by")
    private Long approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @NotNull(message = "Locked flag is required")
    @Column(name = "is_locked", nullable = false)
    private Boolean isLocked = false;

    @NotNull(message = "Active status is required")
    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Lob
    @Column(name = "remarks")
    private String remarks;

    // ==========================================
    // AUDIT & VERSIONING
    // ==========================================
    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    // ==========================================
    // JPA LIFECYCLE CALLBACKS
    // ==========================================
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}