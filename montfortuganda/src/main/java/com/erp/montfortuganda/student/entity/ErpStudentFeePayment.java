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
import java.time.LocalDateTime;

/**
 * Enterprise Fee Payment Receipt Entity.
 * Tracks individual payment transactions against a specific fee assignment.
 */
@Data
@Entity
@DynamicUpdate
@Table(name = "erp_student_fee_payments",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_fee_payment_receipt", columnNames = "receipt_no")
        },
        indexes = {
                @Index(name = "idx_fee_payment_assignment", columnList = "fee_assignment_id"),
                @Index(name = "idx_fee_payment_student", columnList = "student_id"),
                @Index(name = "idx_fee_payment_branch", columnList = "branch_id"),
                @Index(name = "idx_fee_payment_admission", columnList = "admission_no"),
                @Index(name = "idx_fee_payment_date", columnList = "payment_date_time"),
                @Index(name = "idx_fee_payment_status", columnList = "payment_status"),
                @Index(name = "idx_fee_payment_receipt", columnList = "receipt_no")
        }
)
@EqualsAndHashCode(exclude = {"feeAssignment", "student", "branch"})
@ToString(exclude = {"feeAssignment", "student", "branch"})
public class ErpStudentFeePayment implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    // ==========================================
    // ENUMS
    // ==========================================
    public enum PaymentMode {
        CASH, CHEQUE, BANK_TRANSFER, MOBILE_MONEY, CREDIT_CARD, DEBIT_CARD, ONLINE, SCHOLARSHIP, WAIVER
    }

    public enum PaymentStatus {
        PENDING, SUCCESS, FAILED, CANCELLED, REVERSED, REFUNDED
    }

    // ==========================================
    // PRIMARY KEY
    // ==========================================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fee_receipt_id")
    private Long feeReceiptId;

    // ==========================================
    // MASTER REFERENCES
    // ==========================================
    @NotNull(message = "Fee assignment is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fee_assignment_id", nullable = false)
    private ErpStudentFeeAssignment feeAssignment;

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
    // RECEIPT & PAYMENT DETAILS
    // ==========================================
    @NotBlank(message = "Receipt number is required")
    @Size(max = 150, message = "Receipt number cannot exceed 150 characters")
    @Column(name = "receipt_no", nullable = false, length = 150)
    private String receiptNo;

    @NotNull(message = "Payment date and time is required")
    @Column(name = "payment_date_time", nullable = false)
    private LocalDateTime paymentDateTime;

    @NotNull(message = "Payment amount is required")
    @DecimalMin(value = "0.00", message = "Payment amount cannot be negative")
    @Column(name = "payment_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal paymentAmount = BigDecimal.ZERO;

    @NotNull(message = "Excess amount is required")
    @DecimalMin(value = "0.00", message = "Excess amount cannot be negative")
    @Column(name = "excess_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal excessAmount = BigDecimal.ZERO;

    @NotNull(message = "Payment mode is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_mode", nullable = false, length = 30)
    private PaymentMode paymentMode;

    @Size(max = 150)
    @Column(name = "transaction_reference", length = 150)
    private String transactionReference;

    @Size(max = 100)
    @Column(name = "collection_point", length = 100)
    private String collectionPoint;

    @NotNull(message = "Payment status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 20)
    private PaymentStatus paymentStatus = PaymentStatus.SUCCESS;

    @NotNull(message = "Receipt printed flag is required")
    @Column(name = "receipt_printed", nullable = false)
    private Boolean receiptPrinted = false;

    @Column(name = "collected_by")
    private Long collectedBy;

    // ==========================================
    // STATUS & NOTES
    // ==========================================
    @NotNull(message = "Active status is required")
    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Lob
    @Column(name = "remarks", columnDefinition = "LONGTEXT")
    private String remarks;

    @Lob
    @Column(name = "cancel_reason", columnDefinition = "LONGTEXT")
    private String cancelReason;

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

        if (paymentStatus == null) paymentStatus = PaymentStatus.SUCCESS;
        if (receiptPrinted == null) receiptPrinted = false;
        if (active == null) active = true;
        if (version == null) version = 0L;

        if (paymentAmount == null) paymentAmount = BigDecimal.ZERO;
        if (excessAmount == null) excessAmount = BigDecimal.ZERO;

        if (paymentAmount.compareTo(BigDecimal.ZERO) < 0) {
            paymentAmount = BigDecimal.ZERO;
        }

        if (paymentDateTime == null) {
            paymentDateTime = LocalDateTime.now();
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