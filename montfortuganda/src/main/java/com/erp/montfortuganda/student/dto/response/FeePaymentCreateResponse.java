package com.erp.montfortuganda.student.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record FeePaymentCreateResponse(
        Long feeReceiptId,
        String receiptNo,
        LocalDateTime paymentDateTime,
        BigDecimal paymentAmount,
        BigDecimal payableAmount,
        BigDecimal paidAmount,
        BigDecimal outstandingBalance,
        String feeStatus,
        String paymentStatus
) {
}
