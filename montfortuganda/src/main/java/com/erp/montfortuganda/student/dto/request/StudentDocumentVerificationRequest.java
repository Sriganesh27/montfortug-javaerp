package com.erp.montfortuganda.student.dto.request;

import com.erp.montfortuganda.student.entity.ErpStudentDocument.DocumentStatus;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request used by an authorized user to verify or reject
 * an uploaded Student document.
 *
 * Document ID, Student ID, branch, verified-by user and verification
 * timestamp are resolved and controlled by the backend.
 */
@SuppressWarnings("unused")
public record StudentDocumentVerificationRequest(

        @NotNull(message = "Document verification status is required.")
        DocumentStatus documentStatus,

        @Size(
                max = 5000,
                message = "Verification remarks cannot exceed 5000 characters."
        )
        String remarks,

        /**
         * Prevents duplicate verification requests caused by
         * double-clicks or network retries.
         */
        @NotBlank(message = "Operation ID is required.")
        @Size(
                max = 100,
                message = "Operation ID cannot exceed 100 characters."
        )
        String operationId

) {

    /**
     * Manual verification accepts only VERIFIED or REJECTED.
     *
     * PENDING is the initial upload status, while EXPIRED should be
     * assigned by a separate expiry workflow.
     */
    @AssertTrue(
            message = "Document status must be VERIFIED or REJECTED."
    )
    public boolean isVerificationStatusValid() {
        return documentStatus == null
                || documentStatus == DocumentStatus.VERIFIED
                || documentStatus == DocumentStatus.REJECTED;
    }

    /**
     * A rejection must include an explanation.
     */
    @AssertTrue(
            message = "Rejection remarks are required when a document is rejected."
    )
    public boolean isRejectionReasonProvided() {
        return documentStatus != DocumentStatus.REJECTED
                || hasText(remarks);
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}