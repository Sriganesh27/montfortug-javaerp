package com.erp.montfortuganda.student.dto.request;

import com.erp.montfortuganda.student.entity.ErpStudentArchive.ArchiveReason;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Request used to archive a Student after graduation, transfer,
 * withdrawal, expulsion, dropping out, death or another valid reason.
 *
 * Student, branch, admission number, archive status, authenticated user,
 * audit fields and timestamps are controlled by the backend.
 */
@SuppressWarnings("unused")
public record StudentArchiveRequest(

        @NotNull(message = "Archive reason is required.")
        ArchiveReason archiveReason,

        @NotNull(message = "Date of leaving is required.")
        @PastOrPresent(
                message = "Date of leaving cannot be in the future."
        )
        LocalDate dateOfLeaving,

        @Size(
                max = 5000,
                message = "Archive remarks cannot exceed 5000 characters."
        )
        String remarks,

        /**
         * Must match erp_students.version.
         */
        @NotNull(message = "Student record version is required.")
        Long studentVersion,

        /**
         * Prevents duplicate archive operations caused by retries
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