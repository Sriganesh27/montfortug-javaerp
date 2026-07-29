package com.erp.montfortuganda.student.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Request used to convert a graduated Student into an alumni record.
 *
 * Student, branch, admission number, final class, final stream,
 * active status, audit fields and timestamps are controlled by the backend.
 */
@SuppressWarnings("unused")
public record StudentAlumniRequest(

        @NotNull(message = "Graduation year is required.")
        @Min(
                value = 1900,
                message = "Graduation year must be 1900 or later."
        )
        @Max(
                value = 2100,
                message = "Graduation year cannot exceed 2100."
        )
        Integer graduationYear,

        @PastOrPresent(
                message = "Graduation date cannot be in the future."
        )
        LocalDate graduationDate,

        @Size(
                max = 50,
                message = "Final grade cannot exceed 50 characters."
        )
        String finalGrade,

        @Size(
                max = 100,
                message = "Certificate number cannot exceed 100 characters."
        )
        String certificateNumber,

        @Size(
                max = 5000,
                message = "Alumni notes cannot exceed 5000 characters."
        )
        String notes,

        /**
         * Must match erp_students.version.
         */
        @NotNull(message = "Student record version is required.")
        Long studentVersion,

        /**
         * Must match erp_student_enrollment.version.
         */
        @NotNull(message = "Enrollment record version is required.")
        Long enrollmentVersion,

        /**
         * Prevents duplicate alumni conversion caused by retries
         * or repeated button clicks.
         */
        @NotBlank(message = "Operation ID is required.")
        @Size(
                max = 100,
                message = "Operation ID cannot exceed 100 characters."
        )
        String operationId

) {

    /**
     * When a graduation date is supplied, its year must match
     * the selected graduation year.
     */
    @AssertTrue(
            message = "Graduation date must belong to the selected graduation year."
    )
    public boolean isGraduationDateYearValid() {
        return graduationDate == null
                || graduationYear == null
                || graduationDate.getYear() == graduationYear;
    }
}