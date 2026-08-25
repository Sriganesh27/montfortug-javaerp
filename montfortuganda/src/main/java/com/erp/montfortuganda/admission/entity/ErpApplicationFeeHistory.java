package com.erp.montfortuganda.admission.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Immutable
@Table(
        name = "erp_application_fee_history",
        indexes = {
                @Index(
                        name = "idx_fee_history_fee",
                        columnList = "fee_id"
                ),
                @Index(
                        name = "idx_fee_history_application",
                        columnList = "application_id"
                ),
                @Index(
                        name = "idx_fee_history_branch",
                        columnList = "branch_id"
                ),
                @Index(
                        name = "idx_fee_history_changed_at",
                        columnList = "changed_at"
                )
        }
)
public class ErpApplicationFeeHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long historyId;

    @Column(
            name = "fee_id",
            nullable = false
    )
    private Long feeId;

    @Column(
            name = "application_id",
            nullable = false
    )
    private Long applicationId;

    @Column(
            name = "branch_id",
            nullable = false
    )
    private Integer branchId;

    @Column(
            name = "term_fee",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal termFee = BigDecimal.ZERO;

    @Column(
            name = "transport_fee",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal transportFee = BigDecimal.ZERO;

    @Column(
            name = "hostel_fee",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal hostelFee = BigDecimal.ZERO;

    @Column(
            name = "uniform_fee",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal uniformFee = BigDecimal.ZERO;

    @Column(
            name = "books_fee",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal booksFee = BigDecimal.ZERO;

    @Column(
            name = "admission_fee",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal admissionFee = BigDecimal.ZERO;

    @Column(
            name = "other_fee",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal otherFee = BigDecimal.ZERO;

    @Column(
            name = "base_fee_amount",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal baseFeeAmount = BigDecimal.ZERO;

    @Column(
            name = "parent_can_pay",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal parentCanPay = BigDecimal.ZERO;

    @Column(
            name = "assistance_required",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal assistanceRequired = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "fee_decision",
            nullable = false,
            length = 30
    )
    private ErpApplicationFee.FeeDecision feeDecision =
            ErpApplicationFee.FeeDecision.PENDING;

    @Column(
            name = "discussion_remarks",
            length = 500
    )
    private String discussionRemarks;

    @Column(
            name = "scholarship_discount",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal scholarshipDiscount = BigDecimal.ZERO;

    @Column(
            name = "final_payable",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal finalPayable = BigDecimal.ZERO;

    @Column(
            name = "amount_paid",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal amountPaid = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "payment_status",
            length = 30
    )
    private ErpApplicationFee.PaymentStatus paymentStatus =
            ErpApplicationFee.PaymentStatus.PENDING;

    @Column(
            name = "change_reason",
            nullable = false,
            length = 500
    )
    private String changeReason;

    @Column(
            name = "changed_by",
            nullable = false
    )
    private Long changedBy;

    @Column(
            name = "changed_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime changedAt;

    @Column(name = "previous_version")
    private Long previousVersion;

    @PrePersist
    private void onCreate() {
        if (changedAt == null) {
            changedAt = LocalDateTime.now();
        }

        if (termFee == null) {
            termFee = BigDecimal.ZERO;
        }

        if (transportFee == null) {
            transportFee = BigDecimal.ZERO;
        }

        if (hostelFee == null) {
            hostelFee = BigDecimal.ZERO;
        }

        if (uniformFee == null) {
            uniformFee = BigDecimal.ZERO;
        }

        if (booksFee == null) {
            booksFee = BigDecimal.ZERO;
        }

        if (admissionFee == null) {
            admissionFee = BigDecimal.ZERO;
        }

        if (otherFee == null) {
            otherFee = BigDecimal.ZERO;
        }

        if (baseFeeAmount == null) {
            baseFeeAmount = BigDecimal.ZERO;
        }

        if (parentCanPay == null) {
            parentCanPay = BigDecimal.ZERO;
        }

        if (assistanceRequired == null) {
            assistanceRequired = BigDecimal.ZERO;
        }

        if (feeDecision == null) {
            feeDecision =
                    ErpApplicationFee.FeeDecision.PENDING;
        }

        if (scholarshipDiscount == null) {
            scholarshipDiscount = BigDecimal.ZERO;
        }

        if (finalPayable == null) {
            finalPayable = BigDecimal.ZERO;
        }

        if (amountPaid == null) {
            amountPaid = BigDecimal.ZERO;
        }

        if (paymentStatus == null) {
            paymentStatus =
                    ErpApplicationFee.PaymentStatus.PENDING;
        }
    }
}
