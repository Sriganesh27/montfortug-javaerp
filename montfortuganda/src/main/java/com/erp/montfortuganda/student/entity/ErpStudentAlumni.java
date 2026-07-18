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
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Stores records for students who have successfully completed
 * their studies and graduated from the institution.
 */
@Data
@Entity
@DynamicUpdate
@Table(name = "erp_student_alumni",
        indexes = {
                @Index(name = "idx_alumni_student", columnList = "student_id"),
                @Index(name = "idx_alumni_branch", columnList = "branch_id"),
                @Index(name = "idx_alumni_admission", columnList = "admission_no"),
                @Index(name = "idx_alumni_grad_year", columnList = "graduation_year"),
                @Index(name = "idx_alumni_graduation_date", columnList = "graduation_date"),
                @Index(name = "idx_alumni_certificate", columnList = "certificate_number")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_alumni_student", columnNames = "student_id")
        }
)
@EqualsAndHashCode(exclude = {"student", "branch"})
@ToString(exclude = {"student", "branch"})
public class ErpStudentAlumni implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    // ==========================================
    // PRIMARY KEY
    // ==========================================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "alumni_id")
    private Long alumniId;

    // ==========================================
    // OPTIMISTIC LOCKING
    // ==========================================
    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;

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
    // GRADUATION DETAILS
    // ==========================================
    @NotNull(message = "Graduation year is required")
    @Column(name = "graduation_year", nullable = false)
    private Integer graduationYear;

    @Column(name = "graduation_date")
    private LocalDate graduationDate;

    @Size(max = 50, message = "Final class cannot exceed 50 characters")
    @Column(name = "final_class", length = 50)
    private String finalClass;

    @Size(max = 50, message = "Final stream cannot exceed 50 characters")
    @Column(name = "final_stream", length = 50)
    private String finalStream;

    @Size(max = 50, message = "Final grade cannot exceed 50 characters")
    @Column(name = "final_grade", length = 50)
    private String finalGrade;

    @Size(max = 100, message = "Certificate number cannot exceed 100 characters")
    @Column(name = "certificate_number", length = 100)
    private String certificateNumber;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // ==========================================
    // STATUS & AUDIT
    // ==========================================
    @NotNull(message = "Active status is required")
    @Column(name = "active", nullable = false)
    private Boolean active;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_by")
    private Long updatedBy;

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
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}