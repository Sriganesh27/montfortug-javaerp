package com.erp.montfortuganda.admission.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Branch-facing immutable Fee Discussion history snapshot.
 *
 * <p>This DTO is used only to display Fee Discussion history inside the
 * existing Application History workflow view. It does not alter the central
 * application status-history table.</p>
 */
public record ApplicationFeeDiscussionHistoryResponseDTO(
        Long historyId,
        Long feeId,
        Long applicationId,
        Integer branchId,
        BigDecimal termFee,
        BigDecimal transportFee,
        BigDecimal hostelFee,
        BigDecimal uniformFee,
        BigDecimal booksFee,
        BigDecimal admissionFee,
        BigDecimal otherFee,
        BigDecimal baseFeeAmount,
        BigDecimal parentCanPay,
        BigDecimal assistanceRequired,
        String feeDecision,
        String discussionRemarks,
        BigDecimal scholarshipDiscount,
        BigDecimal finalPayable,
        BigDecimal amountPaid,
        String paymentStatus,
        String changeReason,
        Long changedBy,
        LocalDateTime changedAt,
        Long previousVersion
) {
}
