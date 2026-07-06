package com.erp.montfortuganda.admission.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
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

@Data
@Entity
@DynamicUpdate
@Table(name = "erp_application_fees")
@EqualsAndHashCode(exclude = "application")
@ToString(exclude = "application")
public class ErpApplicationFee implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public enum PaymentStatus { PENDING, PARTIAL, PAID, REFUNDED, CANCELLED }
    public enum PaymentMode { CASH, CHEQUE, BANK_TRANSFER, MOBILE_MONEY, CREDIT_CARD, DEBIT_CARD, ONLINE }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fee_id")
    private Long feeId;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "application_id",
            nullable = false,
            unique = true,
            foreignKey = @ForeignKey(name = "fk_application_fee")
    )
    private ErpApplication application;

    @NotNull
    @DecimalMin(value = "0.00")
    @Column(name = "base_fee_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal baseFeeAmount = BigDecimal.ZERO;

    @NotNull
    @DecimalMin(value = "0.00")
    @Column(name = "scholarship_discount", nullable = false, precision = 12, scale = 2)
    private BigDecimal scholarshipDiscount = BigDecimal.ZERO;

    @NotNull
    @DecimalMin(value = "0.00")
    @Column(name = "final_payable", nullable = false, precision = 12, scale = 2)
    private BigDecimal finalPayable = BigDecimal.ZERO;

    @NotNull
    @DecimalMin(value = "0.00")
    @Column(name = "amount_paid", nullable = false, precision = 12, scale = 2)
    private BigDecimal amountPaid = BigDecimal.ZERO;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 30)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_mode", length = 30)
    private PaymentMode paymentMode;

    @Size(max = 150)
    @Column(name = "receipt_no", length = 150, unique = true)
    private String receiptNo;

    @Size(max = 150)
    @Column(name = "receipt_reference", length = 150)
    private String receiptReference;

    @Column(name = "collected_by")
    private Long collectedBy;

    @Size(max = 500)
    @Column(name = "remarks", length = 500)
    private String remarks;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    private void onCreate() {
        if (active == null) {
            active = true;
        }

        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    @PreUpdate
    private void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}