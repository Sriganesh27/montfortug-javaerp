package com.erp.montfortuganda.student.entity;

import com.erp.montfortuganda.school.entity.Branch;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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
 * Enterprise Student Medical Entity.
 * Maintains a strict 1-to-1 relationship with a student, tracking
 * all critical health, biometric, and emergency medical data.
 */
@Data
@Entity
@DynamicUpdate
@Table(name = "erp_student_medical",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_student_medical",
                        columnNames = {"student_id"}
                )
        },
        indexes = {
                @Index(name = "idx_medical_branch", columnList = "branch_id"),
                @Index(name = "idx_medical_admission", columnList = "admission_no"),
                @Index(name = "idx_medical_blood_group", columnList = "blood_group"),
                @Index(name = "idx_medical_sports", columnList = "fit_for_sports")
        }
)
@EqualsAndHashCode(exclude = {"student", "branch"})
@ToString(exclude = {"student", "branch"})
public class ErpStudentMedical implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    // ==========================================
    // ENUMS
    // ==========================================
    public enum BloodGroup {
        A_PLUS("A+"),
        A_MINUS("A-"),
        B_PLUS("B+"),
        B_MINUS("B-"),
        AB_PLUS("AB+"),
        AB_MINUS("AB-"),
        O_PLUS("O+"),
        O_MINUS("O-"),
        UNKNOWN("UNKNOWN");

        private final String code;

        BloodGroup(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }
    }

    // ==========================================
    // PRIMARY KEY
    // ==========================================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "medical_id")
    private Long medicalId;

    // ==========================================
    // STUDENT REFERENCES (1-to-1)
    // ==========================================
    @NotNull(message = "Student is required")
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private ErpStudent student;

    @NotNull(message = "Branch is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @NotBlank(message = "Admission number is required")
    @Size(max = 50, message = "Admission number cannot exceed 50 characters")
    @Column(name = "admission_no", nullable = false, length = 50)
    private String admissionNo;

    // ==========================================
    // VITALS & BIOMETRICS
    // ==========================================
    @NotNull(message = "Blood group is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "blood_group", nullable = false, length = 10)
    private BloodGroup bloodGroup = BloodGroup.UNKNOWN;

    @DecimalMin(value = "0.01", message = "Height must be greater than zero")
    @Column(name = "height_cm", precision = 5, scale = 2)
    private BigDecimal heightCm;

    @DecimalMin(value = "0.01", message = "Weight must be greater than zero")
    @Column(name = "weight_kg", precision = 5, scale = 2)
    private BigDecimal weightKg;

    // ==========================================
    // HEALTH CONDITIONS
    // ==========================================
    @Size(max = 500, message = "Allergies cannot exceed 500 characters")
    @Column(name = "allergies", length = 500)
    private String allergies;

    @Size(max = 500, message = "Chronic conditions cannot exceed 500 characters")
    @Column(name = "chronic_conditions", length = 500)
    private String chronicConditions;

    @Size(max = 500, message = "Ongoing medication cannot exceed 500 characters")
    @Column(name = "ongoing_medication", length = 500)
    private String ongoingMedication;

    @Size(max = 500, message = "Special needs cannot exceed 500 characters")
    @Column(name = "special_needs", length = 500)
    private String specialNeeds;

    // ==========================================
    // CLEARANCES
    // ==========================================
    @NotNull(message = "Sports fitness flag is required")
    @Column(name = "fit_for_sports", nullable = false)
    private Boolean fitForSports = true;

    // ==========================================
    // EMERGENCY CONTACTS
    // ==========================================
    @Size(max = 150, message = "Doctor name cannot exceed 150 characters")
    @Column(name = "emergency_doctor_name", length = 150)
    private String emergencyDoctorName;

    @Pattern(regexp = "^[0-9+\\- ]{7,20}$", message = "Invalid mobile number format")
    @Size(max = 20)
    @Column(name = "emergency_doctor_mobile", length = 20)
    private String emergencyDoctorMobile;

    @Size(max = 150, message = "Hospital name cannot exceed 150 characters")
    @Column(name = "preferred_hospital", length = 150)
    private String preferredHospital;

    // ==========================================
    // STATUS & NOTES
    // ==========================================
    @Size(max = 500, message = "Remarks cannot exceed 500 characters")
    @Column(name = "remarks", length = 500)
    private String remarks;

    @NotNull(message = "Active status is required")
    @Column(name = "active", nullable = false)
    private Boolean active = true;

    // ==========================================
    // AUDIT & LOCKING
    // ==========================================
    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    @Column(name = "created_by", updatable = false)
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
        if (bloodGroup == null) bloodGroup = BloodGroup.UNKNOWN;
        if (fitForSports == null) fitForSports = true;
        if (active == null) active = true;
        if (version == null) version = 0L;

        cleanVitals();

        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        cleanVitals();
        updatedAt = LocalDateTime.now();
    }

    // ==========================================
    // HELPER LOGIC
    // ==========================================
    private void cleanVitals() {
        // Prevent accidental negative insertions via API before it hits the database constraints
        if (heightCm != null && heightCm.signum() <= 0) {
            heightCm = null;
        }
        if (weightKg != null && weightKg.signum() <= 0) {
            weightKg = null;
        }
    }
}