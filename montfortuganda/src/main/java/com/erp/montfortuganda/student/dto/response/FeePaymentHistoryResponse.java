package com.erp.montfortuganda.student.dto.response;

import com.erp.montfortuganda.student.entity.ErpStudentFeePayment.PaymentMode;
import com.erp.montfortuganda.student.entity.ErpStudentFeePayment.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record FeePaymentHistoryResponse(
        Long feeReceiptId,
        String receiptNo,
        Long studentId,
        String admissionNo,
        String studentName,
        Long feeAssignmentId,
        String feeName,
        String feeType,
        String academicYear,
        String term,
        LocalDateTime paymentDateTime,
        BigDecimal paymentAmount,
        BigDecimal excessAmount,
        PaymentMode paymentMode,
        String transactionReference,
        String collectionPoint,
        PaymentStatus paymentStatus,
        boolean receiptPrinted,
        String remarks
) {
}
