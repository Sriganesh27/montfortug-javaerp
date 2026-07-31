package com.erp.montfortuganda.school.dto;

import com.erp.montfortuganda.school.entity.ErpAcademicYear;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Request model for creating or updating a branch-owned Academic Year.
 *
 * <p>Branch ID is intentionally not accepted from the browser. The service
 * must always derive branch ownership from the authenticated user.</p>
 */
public record AcademicYearRequest(

        @NotBlank(
                message = "Academic Year code is required."
        )
        @Size(
                max = 20,
                message = "Academic Year code must not exceed 20 characters."
        )
        @Pattern(
                regexp = "^[A-Za-z0-9][A-Za-z0-9_/-]*$",
                message = "Academic Year code contains unsupported characters."
        )
        String academicYearCode,

        @NotBlank(
                message = "Academic Year name is required."
        )
        @Size(
                max = 100,
                message = "Academic Year name must not exceed 100 characters."
        )
        String academicYearName,

        @NotNull(
                message = "Academic Year start date is required."
        )
        LocalDate startDate,

        @NotNull(
                message = "Academic Year end date is required."
        )
        LocalDate endDate,

        LocalDate admissionStartDate,

        LocalDate admissionEndDate,

        @NotNull(
                message = "Academic Year status is required."
        )
        ErpAcademicYear.Status status,

        Boolean currentYear,

        @Size(
                max = 500,
                message = "Description must not exceed 500 characters."
        )
        String description,

        Boolean active,

        Long version
) {

    @AssertTrue(
            message = "Academic Year end date cannot be before start date."
    )
    public boolean isAcademicYearDateRangeValid() {
        return startDate == null
                || endDate == null
                || !endDate.isBefore(startDate);
    }

    @AssertTrue(
            message = "Admission end date cannot be before admission start date."
    )
    public boolean isAdmissionDateRangeValid() {
        return admissionStartDate == null
                || admissionEndDate == null
                || !admissionEndDate.isBefore(
                admissionStartDate
        );
    }

    @AssertTrue(
            message = "Admission dates must fall within the Academic Year."
    )
    public boolean areAdmissionDatesInsideAcademicYear() {
        if (
                startDate == null
                        || endDate == null
        ) {
            return true;
        }

        if (
                admissionStartDate != null
                        && (
                        admissionStartDate.isBefore(startDate)
                                || admissionStartDate.isAfter(endDate)
                )
        ) {
            return false;
        }

        return admissionEndDate == null
                || (
                !admissionEndDate.isBefore(startDate)
                        && !admissionEndDate.isAfter(endDate)
        );
    }
}