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
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Enterprise Student Hostel Entity.
 * Tracks a student's hostel/boarding enrollment for a given academic year.
 */
@Data
@Entity
@DynamicUpdate
@Table(name = "erp_student_hostel",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_student_hostel_allocation",
                        columnNames = {"student_id", "academic_year"}
                )
        },
        indexes = {
                @Index(name = "idx_hostel_student", columnList = "student_id"),
                @Index(name = "idx_hostel_branch", columnList = "branch_id"),
                @Index(name = "idx_hostel_master", columnList = "hostel_id"),
                @Index(name = "idx_hostel_room", columnList = "room_id"),
                @Index(name = "idx_hostel_bed", columnList = "bed_id"),
                @Index(name = "idx_hostel_status", columnList = "allocation_status"),
                @Index(name = "idx_hostel_payment", columnList = "payment_status"),
                @Index(name = "idx_hostel_year", columnList = "academic_year"),
                @Index(name = "idx_hostel_admission", columnList = "admission_no")
        }
)
@EqualsAndHashCode(exclude = {"student", "branch"})
@ToString(exclude = {"student", "branch"})
public class ErpStudentHostel implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    // ==========================================
    // ENUMS
    // ==========================================
    public enum AllocationStatus {
        ACTIVE, INACTIVE, SUSPENDED, VACATED, CANCELLED
    }

    public enum PaymentStatus {
        PENDING, PARTIAL, PAID
    }

    // ==========================================
    // PRIMARY KEY
    // ==========================================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "hostel_allocation_id")
    private Long hostelAllocationId;

    // ==========================================
    // STUDENT REFERENCES
    // ==========================================
    @NotNull(message = "Student is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
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

    @NotBlank(message = "Academic year is required")
    @Size(max = 20, message = "Academic year cannot exceed 20 characters")
    @Column(name = "academic_year", nullable = false, length = 20)
    private String academicYear;

    // ==========================================
    // HOSTEL MASTER
    // Note: Mapped as Longs until Hostel entities are created.
    // ==========================================
    @NotNull(message = "Hostel ID is required")
    @Column(name = "hostel_id", nullable = false)
    private Long hostelId;

    @Column(name = "room_id")
    private Long roomId;

    @Column(name = "bed_id")
    private Long bedId;

    // ==========================================
    // ENROLLMENT
    // ==========================================
    @NotNull(message = "Allocation start date is required")
    @Column(name = "allocation_start_date", nullable = false)
    private LocalDate allocationStartDate;

    @Column(name = "allocation_end_date")
    private LocalDate allocationEndDate;

    // ==========================================
    // FEES
    // ==========================================
    @NotNull(message = "Monthly fee is required")
    @DecimalMin(value = "0.00", message = "Monthly fee cannot be negative")
    @Column(name = "monthly_fee", nullable = false, precision = 12, scale = 2)
    private BigDecimal monthlyFee = BigDecimal.ZERO;

    @NotNull(message = "Annual fee is required")
    @DecimalMin(value = "0.00", message = "Annual fee cannot be negative")
    @Column(name = "annual_fee", nullable = false, precision = 12, scale = 2)
    private BigDecimal annualFee = BigDecimal.ZERO;

    @NotNull(message = "Discount amount is required")
    @DecimalMin(value = "0.00", message = "Discount amount cannot be negative")
    @Column(name = "discount_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @NotNull(message = "Payable amount is required")
    @DecimalMin(value = "0.00", message = "Payable amount cannot be negative")
    @Column(name = "payable_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal payableAmount = BigDecimal.ZERO;

    // ==========================================
    // STATUS
    // ==========================================
    @NotNull(message = "Allocation status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "allocation_status", nullable = false, length = 20)
    private AllocationStatus allocationStatus = AllocationStatus.ACTIVE;

    @NotNull(message = "Payment status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 20)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    // ==========================================
    // GUARDIAN & NOTES
    // ==========================================
    @Size(max = 150, message = "Local guardian name cannot exceed 150 characters")
    @Column(name = "local_guardian_name", length = 150)
    private String localGuardianName;

    @Pattern(regexp = "^[0-9+\\- ]{7,20}$", message = "Invalid mobile number format")
    @Size(max = 20)
    @Column(name = "local_guardian_mobile", length = 20)
    private String localGuardianMobile;

    @Size(max = 50, message = "Relation cannot exceed 50 characters")
    @Column(name = "local_guardian_relation", length = 50)
    private String localGuardianRelation;

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
        if (allocationStatus == null) allocationStatus = AllocationStatus.ACTIVE;
        if (paymentStatus == null) paymentStatus = PaymentStatus.PENDING;
        if (active == null) active = true;
        if (version == null) version = 0L;

        if (allocationStartDate == null) {
            allocationStartDate = LocalDate.now();
        }

        recalculateAmounts();
        validateDates();

        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        recalculateAmounts();
        validateDates();
        updatedAt = LocalDateTime.now();
    }

    // ==========================================
    // HELPER LOGIC
    // ==========================================
    private void recalculateAmounts() {
        if (monthlyFee == null) monthlyFee = BigDecimal.ZERO;
        if (annualFee == null) annualFee = BigDecimal.ZERO;
        if (discountAmount == null) discountAmount = BigDecimal.ZERO;

        if (monthlyFee.compareTo(BigDecimal.ZERO) < 0) monthlyFee = BigDecimal.ZERO;
        if (annualFee.compareTo(BigDecimal.ZERO) < 0) annualFee = BigDecimal.ZERO;
        if (discountAmount.compareTo(BigDecimal.ZERO) < 0) discountAmount = BigDecimal.ZERO;

        payableAmount = annualFee.subtract(discountAmount);

        if (payableAmount.signum() < 0) {
            payableAmount = BigDecimal.ZERO;
        }
    }

    private void validateDates() {
        if (allocationEndDate != null && allocationEndDate.isBefore(allocationStartDate)) {
            throw new IllegalArgumentException("Hostel allocation end date cannot be before start date");
        }
    }
}