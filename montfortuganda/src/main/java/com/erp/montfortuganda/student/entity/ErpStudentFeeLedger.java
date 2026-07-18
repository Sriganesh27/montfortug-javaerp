package com.erp.montfortuganda.student.entity;

import com.erp.montfortuganda.school.entity.Branch;
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
 * Enterprise Fee Ledger Entity.
 * An immutable, double-entry event log for all financial transactions,
 * providing a perfect audit trail and running balance for every fee.
 */
@Data
@Entity
@DynamicUpdate
@Table(name = "erp_student_fee_ledger",
        indexes = {
                @Index(name = "idx_fee_ledger_assignment", columnList = "fee_assignment_id"),
                @Index(name = "idx_fee_ledger_receipt", columnList = "fee_receipt_id"),
                @Index(name = "idx_fee_ledger_student", columnList = "student_id"),
                @Index(name = "idx_fee_ledger_branch", columnList = "branch_id"),
                @Index(name = "idx_fee_ledger_admission", columnList = "admission_no"),
                @Index(name = "idx_fee_ledger_transaction", columnList = "transaction_type"),
                @Index(name = "idx_fee_ledger_date", columnList = "transaction_date_time"),
                @Index(name = "idx_fee_ledger_status", columnList = "ledger_status"),
                @Index(name = "idx_fee_ledger_year_term", columnList = "academic_year, term"),
                @Index(name = "idx_fee_ledger_fee_name", columnList = "fee_name"),
                @Index(name = "idx_fee_ledger_student_date", columnList = "student_id, transaction_date_time"),
                @Index(name = "idx_fee_ledger_assignment_date", columnList = "fee_assignment_id, transaction_date_time")
        }
)
@EqualsAndHashCode(exclude = {"feeAssignment", "feeReceipt", "student", "branch"})
@ToString(exclude = {"feeAssignment", "feeReceipt", "student", "branch"})
public class ErpStudentFeeLedger implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    // ==========================================
    // ENUMS
    // ==========================================
    public enum TransactionType {
        FEE_ASSIGNED, PAYMENT, PARTIAL_PAYMENT, SCHOLARSHIP, CONCESSION, DISCOUNT, FINE, WAIVER, REFUND, REVERSAL, ADJUSTMENT
    }

    public enum PaymentMode {
        CASH, CHEQUE, BANK_TRANSFER, MOBILE_MONEY, CREDIT_CARD, DEBIT_CARD, ONLINE, SCHOLARSHIP, WAIVER
    }

    public enum LedgerStatus {
        ACTIVE, CANCELLED, REVERSED
    }

    // ==========================================
    // PRIMARY KEY
    // ==========================================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fee_ledger_id")
    private Long feeLedgerId;

    // ==========================================
    // MASTER REFERENCES
    // ==========================================
    @NotNull(message = "Fee assignment is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fee_assignment_id", nullable = false)
    private ErpStudentFeeAssignment feeAssignment;

    /**
     * Nullable because initial FEE_ASSIGNED events or FINES do not have a receipt.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fee_receipt_id")
    private ErpStudentFeePayment feeReceipt;

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

    @NotBlank(message = "Term is required")
    @Size(max = 30, message = "Term cannot exceed 30 characters")
    @Column(name = "term", nullable = false, length = 30)
    private String term;

    @NotBlank(message = "Fee name is required")
    @Size(max = 150, message = "Fee name cannot exceed 150 characters")
    @Column(name = "fee_name", nullable = false, length = 150)
    private String feeName;

    @NotBlank(message = "Fee type is required")
    @Size(max = 50, message = "Fee type cannot exceed 50 characters")
    @Column(name = "fee_type", nullable = false, length = 50)
    private String feeType;

    // ==========================================
    // TRANSACTION INFORMATION
    // ==========================================
    @NotNull(message = "Transaction type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 30)
    private TransactionType transactionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_mode", length = 30)
    private PaymentMode paymentMode;

    @Size(max = 150)
    @Column(name = "transaction_reference", length = 150)
    private String transactionReference;

    @NotNull(message = "Transaction date and time is required")
    @Column(name = "transaction_date_time", nullable = false)
    private LocalDateTime transactionDateTime;

    // ==========================================
    // FINANCIAL DETAILS
    // ==========================================
    @NotNull(message = "Debit amount is required")
    @DecimalMin(value = "0.00", message = "Debit amount cannot be negative")
    @Column(name = "debit_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal debitAmount = BigDecimal.ZERO;

    @NotNull(message = "Credit amount is required")
    @DecimalMin(value = "0.00", message = "Credit amount cannot be negative")
    @Column(name = "credit_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal creditAmount = BigDecimal.ZERO;

    /**
     * Running balance.
     * Can be negative if a parent overpays (creating an advance credit balance).
     */
    @NotNull(message = "Running balance is required")
    @Column(name = "running_balance", nullable = false, precision = 12, scale = 2)
    private BigDecimal runningBalance;

    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be exactly 3 characters")
    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "UGX";

    // ==========================================
    // STATUS
    // ==========================================
    @NotNull(message = "Ledger status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "ledger_status", nullable = false, length = 20)
    private LedgerStatus ledgerStatus = LedgerStatus.ACTIVE;

    @Lob
    @Column(name = "remarks", columnDefinition = "TEXT")
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

        if (ledgerStatus == null) ledgerStatus = LedgerStatus.ACTIVE;
        if (version == null) version = 0L;
        if (currency == null) currency = "UGX";

        if (debitAmount == null) debitAmount = BigDecimal.ZERO;
        if (creditAmount == null) creditAmount = BigDecimal.ZERO;

        if (debitAmount.compareTo(BigDecimal.ZERO) < 0) debitAmount = BigDecimal.ZERO;
        if (creditAmount.compareTo(BigDecimal.ZERO) < 0) creditAmount = BigDecimal.ZERO;

        if (transactionDateTime == null) {
            transactionDateTime = LocalDateTime.now();
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