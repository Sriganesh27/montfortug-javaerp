package com.erp.montfortuganda.student.entity;

import com.erp.montfortuganda.school.Branch;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Stores the parent/guardian demographic and contact information for a specific student.
 * Maps strictly 1-to-1 with a Student.
 */
@Data
@Entity
@DynamicUpdate
@Table(name = "erp_parents",
        indexes = {
                @Index(name = "idx_parent_branch", columnList = "branch_id"),
                @Index(name = "idx_parent_admission", columnList = "admission_no"),

                @Index(name = "idx_father_phone", columnList = "father_phone"),
                @Index(name = "idx_mother_phone", columnList = "mother_phone"),
                @Index(name = "idx_guardian_phone", columnList = "guardian_phone"),

                @Index(name = "idx_father_email", columnList = "father_email"),
                @Index(name = "idx_mother_email", columnList = "mother_email"),
                @Index(name = "idx_guardian_email", columnList = "guardian_email"),

                @Index(name = "idx_father_uin", columnList = "father_uin"),
                @Index(name = "idx_mother_uin", columnList = "mother_uin"),
                @Index(name = "idx_guardian_uin", columnList = "guardian_uin"),

                @Index(name = "idx_emergency_phone", columnList = "emergency_contact_phone"),
                @Index(name = "idx_active", columnList = "active")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_parent_student", columnNames = "student_id")
        }
)
@EqualsAndHashCode(exclude = {"student", "branch"})
@ToString(exclude = {"student", "branch"})
public class ErpParent implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    // ==========================================
    // NESTED ENUMS
    // ==========================================
    public enum PreferredContact {
        FATHER, MOTHER, GUARDIAN
    }

    public enum FeeResponsibility {
        FATHER, MOTHER, GUARDIAN, SPONSOR
    }

    // ==========================================
    // PRIMARY KEY
    // ==========================================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "parent_id")
    private Long parentId;

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
    // FATHER DETAILS
    // ==========================================
    @Size(max = 150, message = "Father name cannot exceed 150 characters")
    @Column(name = "father_name", length = 150)
    private String fatherName;

    @Size(max = 20, message = "Father UIN cannot exceed 20 characters")
    @Column(name = "father_uin", length = 20)
    private String fatherUin;

    @Size(max = 30, message = "Father phone cannot exceed 30 characters")
    @Column(name = "father_phone", length = 30)
    private String fatherPhone;

    @Size(max = 30, message = "Father alternate phone cannot exceed 30 characters")
    @Column(name = "father_alternate_phone", length = 30)
    private String fatherAlternatePhone;

    @Size(max = 150, message = "Father email cannot exceed 150 characters")
    @Column(name = "father_email", length = 150)
    private String fatherEmail;

    @Size(max = 150, message = "Father occupation cannot exceed 150 characters")
    @Column(name = "father_occupation", length = 150)
    private String fatherOccupation;

    @Size(max = 200, message = "Father employer cannot exceed 200 characters")
    @Column(name = "father_employer", length = 200)
    private String fatherEmployer;

    @Size(max = 150, message = "Father designation cannot exceed 150 characters")
    @Column(name = "father_designation", length = 150)
    private String fatherDesignation;

    @Column(name = "father_annual_income", precision = 15, scale = 2)
    private BigDecimal fatherAnnualIncome;

    // ==========================================
    // MOTHER DETAILS
    // ==========================================
    @Size(max = 150, message = "Mother name cannot exceed 150 characters")
    @Column(name = "mother_name", length = 150)
    private String motherName;

    @Size(max = 20, message = "Mother UIN cannot exceed 20 characters")
    @Column(name = "mother_uin", length = 20)
    private String motherUin;

    @Size(max = 30, message = "Mother phone cannot exceed 30 characters")
    @Column(name = "mother_phone", length = 30)
    private String motherPhone;

    @Size(max = 30, message = "Mother alternate phone cannot exceed 30 characters")
    @Column(name = "mother_alternate_phone", length = 30)
    private String motherAlternatePhone;

    @Size(max = 150, message = "Mother email cannot exceed 150 characters")
    @Column(name = "mother_email", length = 150)
    private String motherEmail;

    @Size(max = 150, message = "Mother occupation cannot exceed 150 characters")
    @Column(name = "mother_occupation", length = 150)
    private String motherOccupation;

    @Size(max = 200, message = "Mother employer cannot exceed 200 characters")
    @Column(name = "mother_employer", length = 200)
    private String motherEmployer;

    @Size(max = 150, message = "Mother designation cannot exceed 150 characters")
    @Column(name = "mother_designation", length = 150)
    private String motherDesignation;

    @Column(name = "mother_annual_income", precision = 15, scale = 2)
    private BigDecimal motherAnnualIncome;

    // ==========================================
    // GUARDIAN DETAILS
    // ==========================================
    @Size(max = 150, message = "Guardian name cannot exceed 150 characters")
    @Column(name = "guardian_name", length = 150)
    private String guardianName;

    @Size(max = 20, message = "Guardian UIN cannot exceed 20 characters")
    @Column(name = "guardian_uin", length = 20)
    private String guardianUin;

    @Size(max = 100, message = "Guardian relationship cannot exceed 100 characters")
    @Column(name = "guardian_relationship", length = 100)
    private String guardianRelationship;

    @Size(max = 30, message = "Guardian phone cannot exceed 30 characters")
    @Column(name = "guardian_phone", length = 30)
    private String guardianPhone;

    @Size(max = 30, message = "Guardian alternate phone cannot exceed 30 characters")
    @Column(name = "guardian_alternate_phone", length = 30)
    private String guardianAlternatePhone;

    @Size(max = 150, message = "Guardian email cannot exceed 150 characters")
    @Column(name = "guardian_email", length = 150)
    private String guardianEmail;

    @Size(max = 150, message = "Guardian occupation cannot exceed 150 characters")
    @Column(name = "guardian_occupation", length = 150)
    private String guardianOccupation;

    // ==========================================
    // COMMUNICATION & FAMILY CONTEXT
    // ==========================================
    @NotNull(message = "Preferred contact is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_contact", nullable = false, length = 20)
    private PreferredContact preferredContact;

    @NotNull(message = "Fee responsibility is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "fee_responsibility", nullable = false, length = 20)
    private FeeResponsibility feeResponsibility;

    @Column(name = "parents_living_together")
    private Boolean parentsLivingTogether;

    @Size(max = 150, message = "Emergency contact name cannot exceed 150 characters")
    @Column(name = "emergency_contact_name", length = 150)
    private String emergencyContactName;

    @Size(max = 30, message = "Emergency contact phone cannot exceed 30 characters")
    @Column(name = "emergency_contact_phone", length = 30)
    private String emergencyContactPhone;

    @Size(max = 100, message = "Emergency contact relationship cannot exceed 100 characters")
    @Column(name = "emergency_contact_relationship", length = 100)
    private String emergencyContactRelationship;

    // ==========================================
    // STATUS & AUDIT
    // ==========================================
    @NotNull(message = "Active status is required")
    @Column(name = "active", nullable = false)
    private Boolean active;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

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
        if (preferredContact == null) {
            preferredContact = PreferredContact.FATHER;
        }
        if (feeResponsibility == null) {
            feeResponsibility = FeeResponsibility.FATHER;
        }
        if (parentsLivingTogether == null) {
            parentsLivingTogether = true;
        }
        if (version == null) {
            version = 0L;
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
