package com.erp.montfortuganda.admission.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ApplicationFeeDiscussionResponseDTO(
        Long feeId,
        Long applicationId,
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
        LocalDateTime discussionDate,
        String discussionRemarks,
        BigDecimal scholarshipDiscount,
        BigDecimal finalPayable,
        BigDecimal amountPaid,
        String paymentStatus,
        String remarks
) {
}
