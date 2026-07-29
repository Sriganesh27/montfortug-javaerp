package com.erp.montfortuganda.student.dto.request;

import com.erp.montfortuganda.student.enums.StudentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Request used for controlled Student status changes.
 *
 * Student ID, branch, current status, active flag, changed-by user,
 * audit fields and timestamps are controlled by the backend.
 */
@SuppressWarnings("unused")
public record StudentStatusChangeRequest(

        @NotNull(message = "New Student status is required.")
        StudentStatus newStatus,

        @NotNull(message = "Status effective date is required.")
        @PastOrPresent(message = "Status effective date cannot be in the future.")
        LocalDate effectiveDate,

        @NotBlank(message = "Status-change reason is required.")
        @Size(
                max = 500,
                message = "Status-change reason cannot exceed 500 characters."
        )
        String reason,

        /*
         * Required for optimistic locking.
         * Must match erp_students.version.
         */
        @NotNull(message = "Student record version is required.")
        Long version,

        /*
         * Prevents duplicate status changes caused by double-clicks
         * or network retries.
         */
        @NotBlank(message = "Operation ID is required.")
        @Size(
                max = 100,
                message = "Operation ID cannot exceed 100 characters."
        )
        String operationId

) {
}