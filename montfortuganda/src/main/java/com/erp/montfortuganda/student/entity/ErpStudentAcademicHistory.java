package com.erp.montfortuganda.student.entity;

import com.erp.montfortuganda.school.entity.Branch;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Stores the student's academic background prior to admission.

 * One student can have only one academic history record.
 * Historical enrollments after admission are maintained in
 * erp_student_enrollment_history.
 */
@Data
@Entity
@DynamicUpdate
@Table(name = "erp_student_academic_history",
        indexes = {
                @Index(name = "idx_academic_student", columnList = "student_id"),
                @Index(name = "idx_academic_branch", columnList = "branch_id"),
                @Index(name = "idx_academic_admission", columnList = "admission_no"),
                @Index(name = "idx_academic_school", columnList = "former_school_code"),
                @Index(name = "idx_academic_verification", columnList = "verification_status"),
                @Index(name = "idx_ple", columnList = "ple_index_number"),
                @Index(name = "idx_uce", columnList = "uce_index_number"),
                @Index(name = "idx_uace", columnList = "uace_index_number")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_student_academic_history", columnNames = "student_id")
        }
)
@EqualsAndHashCode(exclude = {"student", "branch"})
@ToString(exclude = {"student", "branch"})
public class ErpStudentAcademicHistory implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    // ==========================================
    // NESTED ENUMS
    // ==========================================
    public enum SchoolType {
        GOVERNMENT, PRIVATE, INTERNATIONAL, OTHER
    }

    public enum VerificationStatus {
        PENDING, VERIFIED, REJECTED
    }

    // ==========================================
    // PRIMARY KEY
    // ==========================================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "academic_history_id")
    private Long academicHistoryId;

    // ==========================================
    // OPTIMISTIC LOCKING
    // ==========================================
    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    // ==========================================
    // MASTER REFERENCES
    // ==========================================
    @NotNull(message = "Student is required")
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private ErpStudent student;

    @NotNull(message = "Branch is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @NotNull(message = "Admission number is required")
    @Size(max = 50, message = "Admission number cannot exceed 50 characters")
    @Column(name = "admission_no", nullable = false, length = 50)
    private String admissionNo;

    // ==========================================
    // PREVIOUS SCHOOL INFORMATION
    // ==========================================
    @Size(max = 255, message = "School name cannot exceed 255 characters")
    @Column(name = "former_school_name", length = 255)
    private String formerSchoolName;

    @Size(max = 50, message = "School code cannot exceed 50 characters")
    @Column(name = "former_school_code", length = 50)
    private String formerSchoolCode;

    @Size(max = 50, message = "School LIN cannot exceed 50 characters")
    @Column(name = "former_school_lin", length = 50)
    private String formerSchoolLin;

    @Size(max = 255, message = "School address cannot exceed 255 characters")
    @Column(name = "former_school_address", length = 255)
    private String formerSchoolAddress;

    @Enumerated(EnumType.STRING)
    @Column(name = "school_type", length = 30)
    private SchoolType schoolType; // Removed redundant default

    @Size(max = 255, message = "Transfer reason cannot exceed 255 characters")
    @Column(name = "transfer_reason", length = 255)
    private String transferReason;

    // ==========================================
    // LAST CLASS INFORMATION
    // ==========================================
    @Size(max = 20, message = "Previous academic year cannot exceed 20 characters")
    @Column(name = "previous_academic_year", length = 20)
    private String previousAcademicYear;

    @Size(max = 50, message = "Previous class cannot exceed 50 characters")
    @Column(name = "previous_class", length = 50)
    private String previousClass;

    @Size(max = 50, message = "Previous section cannot exceed 50 characters")
    @Column(name = "previous_section", length = 50)
    private String previousSection;

    @Size(max = 50, message = "Previous stream cannot exceed 50 characters")
    @Column(name = "previous_stream", length = 50)
    private String previousStream;

    // ==========================================
    // PLE DETAILS
    // ==========================================
    @Size(max = 50, message = "PLE index number cannot exceed 50 characters")
    @Column(name = "ple_index_number", length = 50)
    private String pleIndexNumber;

    @Size(max = 20, message = "PLE aggregate cannot exceed 20 characters")
    @Column(name = "ple_aggregate", length = 20)
    private String pleAggregate;

    // ==========================================
    // UCE DETAILS
    // ==========================================
    @Size(max = 50, message = "UCE index number cannot exceed 50 characters")
    @Column(name = "uce_index_number", length = 50)
    private String uceIndexNumber;

    @Size(max = 50, message = "UCE result cannot exceed 50 characters")
    @Column(name = "uce_result", length = 50)
    private String uceResult;

    // ==========================================
    // UACE DETAILS
    // ==========================================
    @Size(max = 50, message = "UACE index number cannot exceed 50 characters")
    @Column(name = "uace_index_number", length = 50)
    private String uaceIndexNumber;

    @Size(max = 50, message = "UACE result cannot exceed 50 characters")
    @Column(name = "uace_result", length = 50)
    private String uaceResult;

    // ==========================================
    // SUBJECT PERFORMANCE
    // ==========================================
    @Column(name = "subject_marks", columnDefinition = "LONGTEXT")
    private String subjectMarks;

    // ==========================================
    // DOCUMENTS
    // ==========================================
    @Size(max = 255, message = "Previous report card path cannot exceed 255 characters")
    @Column(name = "previous_report_card", length = 255)
    private String previousReportCard;

    @Size(max = 255, message = "Transfer certificate path cannot exceed 255 characters")
    @Column(name = "transfer_certificate", length = 255)
    private String transferCertificate;

    @Size(max = 255, message = "Leaving certificate path cannot exceed 255 characters")
    @Column(name = "leaving_certificate", length = 255)
    private String leavingCertificate;

    // ==========================================
    // VERIFICATION
    // ==========================================
    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", length = 30, nullable = false)
    private VerificationStatus verificationStatus; // Removed redundant default

    @Column(name = "verified_by")
    private Long verifiedBy;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    // ==========================================
    // STATUS & AUDIT
    // ==========================================
    @NotNull(message = "Active status is required")
    @Column(name = "active", nullable = false)
    private Boolean active; // Removed redundant default

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ==========================================
    // JPA LIFECYCLE CALLBACKS
    // ==========================================
    @PrePersist
    protected void onCreate() {
        if (active == null) {
            active = true;
        }
        if (schoolType == null) {
            schoolType = SchoolType.PRIVATE;
        }
        if (verificationStatus == null) {
            verificationStatus = VerificationStatus.PENDING;
        }
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}