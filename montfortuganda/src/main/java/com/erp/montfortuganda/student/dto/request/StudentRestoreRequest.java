package com.erp.montfortuganda.student.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request used to restore an archived Student.
 *
 * Archive status, restored-by user, restored timestamp, branch,
 * Student status and audit values are controlled by the backend.
 */
@SuppressWarnings("unused")
public record StudentRestoreRequest(

        @NotBlank(message = "Restore reason is required.")
        @Size(
                max = 255,
                message = "Restore reason cannot exceed 255 characters."
        )
        String restoreReason,

        /**
         * Must match erp_student_archives.version.
         */
        @NotNull(message = "Archive record version is required.")
        Long archiveVersion,

        /**
         * Must match erp_students.version.
         */
        @NotNull(message = "Student record version is required.")
        Long studentVersion,

        /**
         * Prevents duplicate restore operations caused by retries
         * or repeated button clicks.
         */
        @NotBlank(message = "Operation ID is required.")
        @Size(
                max = 100,
                message = "Operation ID cannot exceed 100 characters."
        )
        String operationId

) {
}