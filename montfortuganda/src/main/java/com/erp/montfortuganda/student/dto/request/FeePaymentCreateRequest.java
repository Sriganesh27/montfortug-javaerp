package com.erp.montfortuganda.student.dto.request;

import com.erp.montfortuganda.student.entity.ErpStudentFeePayment;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record FeePaymentCreateRequest(
        @NotNull(message = "Payment amount is required")
        @DecimalMin(value = "0.01", message = "Payment amount must be greater than zero")
        BigDecimal amount,

        @NotNull(message = "Payment mode is required")
        ErpStudentFeePayment.PaymentMode paymentMode,

        @Size(max = 150, message = "Transaction reference cannot exceed 150 characters")
        String transactionReference,

        @Size(max = 100, message = "Collection point cannot exceed 100 characters")
        String collectionPoint,

        String remarks
) {
}
