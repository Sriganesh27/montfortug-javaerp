package com.erp.montfortuganda.student.entity;

import com.erp.montfortuganda.school.Branch;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
 * Enterprise Fee Assignment Entity.
 * Tracks individual fee ledger items assigned to a student.
 */
@Data
@Entity
@DynamicUpdate
@Table(name = "erp_student_fee_assignments",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_student_fee_assignment",
                        columnNames = {
                                "student_id",
                                "academic_year",
                                "term",
                                "fee_name"
                        }
                )
        },
        indexes = {
                @Index(name = "idx_fee_assignment_student", columnList = "student_id"),
                @Index(name = "idx_fee_assignment_branch", columnList = "branch_id"),
                @Index(name = "idx_fee_assignment_admission", columnList = "admission_no"),
                @Index(name = "idx_fee_assignment_year", columnList = "academic_year"),
                @Index(name = "idx_fee_assignment_term", columnList = "term"),
                @Index(name = "idx_fee_assignment_status", columnList = "fee_status"),
                @Index(name = "idx_fee_assignment_due_date", columnList = "due_date"),
                @Index(name = "idx_fee_assignment_fee_name", columnList = "fee_name")
        }
)
@EqualsAndHashCode(exclude = {"student", "branch"})
@ToString(exclude = {"student", "branch"})
public class ErpStudentFeeAssignment implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    // ==========================================
    // ENUMS
    // ==========================================
    public enum FeeStatus {
        PENDING, PARTIAL, PAID, OVERDUE, CANCELLED
    }

    // ==========================================
    // PRIMARY KEY
    // ==========================================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fee_assignment_id")
    private Long feeAssignmentId;

    // ==========================================
    // MASTER REFERENCES
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

    // ==========================================
    // ACADEMIC INFORMATION
    // ==========================================
    @NotBlank(message = "Academic year is required")
    @Size(max = 20, message = "Academic year cannot exceed 20 characters")
    @Column(name = "academic_year", nullable = false, length = 20)
    private String academicYear;

    @NotBlank(message = "Term is required")
    @Size(max = 30, message = "Term cannot exceed 30 characters")
    @Column(name = "term", nullable = false, length = 30)
    private String term;

    // ==========================================
    // FEE INFORMATION
    // ==========================================
    @NotBlank(message = "Fee name is required")
    @Size(max = 150, message = "Fee name cannot exceed 150 characters")
    @Column(name = "fee_name", nullable = false, length = 150)
    private String feeName;

    @NotBlank(message = "Fee type is required")
    @Size(max = 50, message = "Fee type cannot exceed 50 characters")
    @Column(name = "fee_type", nullable = false, length = 50)
    private String feeType;

    @NotNull(message = "Total fee is required")
    @DecimalMin(value = "0.00", message = "Total fee cannot be negative")
    @Column(name = "total_fee", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalFee;

    @NotNull(message = "Scholarship amount is required")
    @DecimalMin(value = "0.00", message = "Scholarship amount cannot be negative")
    @Column(name = "scholarship_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal scholarshipAmount = BigDecimal.ZERO;

    @NotNull(message = "Concession amount is required")
    @DecimalMin(value = "0.00", message = "Concession amount cannot be negative")
    @Column(name = "concession_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal concessionAmount = BigDecimal.ZERO;

    @NotNull(message = "Discount amount is required")
    @DecimalMin(value = "0.00", message = "Discount amount cannot be negative")
    @Column(name = "discount_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @NotNull(message = "Fine amount is required")
    @DecimalMin(value = "0.00", message = "Fine amount cannot be negative")
    @Column(name = "fine_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal fineAmount = BigDecimal.ZERO;

    @NotNull(message = "Payable amount is required")
    @DecimalMin(value = "0.00", message = "Payable amount cannot be negative")
    @Column(name = "payable_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal payableAmount;

    @NotNull(message = "Paid amount is required")
    @DecimalMin(value = "0.00", message = "Paid amount cannot be negative")
    @Column(name = "paid_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @NotNull(message = "Balance amount is required")
    @DecimalMin(value = "0.00", message = "Balance amount cannot be negative")
    @Column(name = "balance_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal balanceAmount;

    // ==========================================
    // DATES & STATUS
    // ==========================================
    @NotNull(message = "Assignment date is required")
    @Column(name = "assignment_date", nullable = false)
    private LocalDate assignmentDate;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @NotNull(message = "Fee status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "fee_status", nullable = false, length = 20)
    private FeeStatus feeStatus = FeeStatus.PENDING;

    @NotNull(message = "Active status is required")
    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Lob
    @Column(name = "remarks")
    private String remarks;

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
        if (feeStatus == null) feeStatus = FeeStatus.PENDING;
        if (active == null) active = true;
        if (version == null) version = 0L;

        recalculateAmounts();

        if (assignmentDate == null) {
            assignmentDate = LocalDate.now();
        }

        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        recalculateAmounts();
        updatedAt = LocalDateTime.now();
    }

    // ==========================================
    // HELPER LOGIC
    // ==========================================
    private void recalculateAmounts() {
        if (totalFee == null) {
            return;
        }

        scholarshipAmount = scholarshipAmount == null ? BigDecimal.ZERO : scholarshipAmount;
        concessionAmount = concessionAmount == null ? BigDecimal.ZERO : concessionAmount;
        discountAmount = discountAmount == null ? BigDecimal.ZERO : discountAmount;
        fineAmount = fineAmount == null ? BigDecimal.ZERO : fineAmount;
        paidAmount = paidAmount == null ? BigDecimal.ZERO : paidAmount;

        payableAmount = totalFee
                .subtract(scholarshipAmount)
                .subtract(concessionAmount)
                .subtract(discountAmount)
                .add(fineAmount);

        if (payableAmount.compareTo(BigDecimal.ZERO) < 0) {
            payableAmount = BigDecimal.ZERO;
        }

        balanceAmount = payableAmount.subtract(paidAmount);

        if (balanceAmount.compareTo(BigDecimal.ZERO) < 0) {
            balanceAmount = BigDecimal.ZERO;
        }

        updateFeeStatus();
    }

    private void updateFeeStatus() {
        // Do not auto-update if an admin has manually cancelled the fee
        if (feeStatus == FeeStatus.CANCELLED) {
            return;
        }

        if (balanceAmount.compareTo(BigDecimal.ZERO) == 0) {
            feeStatus = FeeStatus.PAID;
        } else if (paidAmount.compareTo(BigDecimal.ZERO) > 0) {
            feeStatus = FeeStatus.PARTIAL;
        } else if (dueDate != null && dueDate.isBefore(LocalDate.now())) {
            feeStatus = FeeStatus.OVERDUE;
        } else {
            feeStatus = FeeStatus.PENDING;
        }
    }
}