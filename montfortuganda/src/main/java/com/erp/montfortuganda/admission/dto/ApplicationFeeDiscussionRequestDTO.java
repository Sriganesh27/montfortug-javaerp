package com.erp.montfortuganda.admission.dto;

import com.erp.montfortuganda.admission.entity.ErpApplicationFee;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ApplicationFeeDiscussionRequestDTO(

        @DecimalMin(value = "0.00", inclusive = true)
        BigDecimal termFee,

        @DecimalMin(value = "0.00", inclusive = true)
        BigDecimal transportFee,

        @DecimalMin(value = "0.00", inclusive = true)
        BigDecimal hostelFee,

        @DecimalMin(value = "0.00", inclusive = true)
        BigDecimal uniformFee,

        @DecimalMin(value = "0.00", inclusive = true)
        BigDecimal booksFee,

        @DecimalMin(value = "0.00", inclusive = true)
        BigDecimal admissionFee,

        @DecimalMin(value = "0.00", inclusive = true)
        BigDecimal otherFee,

        @DecimalMin(value = "0.00", inclusive = true)
        BigDecimal parentCanPay,

        ErpApplicationFee.FeeDecision feeDecision,

        /*
         * Kept for API backward compatibility.
         * The backend currently stamps discussionDate automatically.
         */
        LocalDateTime discussionDate,

        @Size(max = 500)
        String discussionRemarks,

        /*
         * Required by the service only when an existing fee record is edited.
         * First-time fee discussion creation does not require a change reason.
         */
        @Size(max = 500)
        String changeReason
) {
}
