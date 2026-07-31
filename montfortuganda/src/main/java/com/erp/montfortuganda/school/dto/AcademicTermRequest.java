package com.erp.montfortuganda.school.dto;

import com.erp.montfortuganda.school.entity.ErpAcademicTerm;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Request model for creating or updating an Academic Term.
 *
 * <p>The Academic Year ID is accepted because the Term must belong to one
 * specific Academic Year. Branch ID is intentionally excluded. The service
 * must verify that the selected Academic Year belongs to the authenticated
 * branch.</p>
 */
public record AcademicTermRequest(

        @NotNull(
                message = "Academic Year is required."
        )
        @Positive(
                message = "Academic Year ID must be greater than zero."
        )
        Long academicYearId,

        @NotBlank(
                message = "Term code is required."
        )
        @Size(
                max = 20,
                message = "Term code must not exceed 20 characters."
        )
        @Pattern(
                regexp = "^[A-Za-z0-9][A-Za-z0-9_/-]*$",
                message = "Term code contains unsupported characters."
        )
        String termCode,

        @NotBlank(
                message = "Term name is required."
        )
        @Size(
                max = 100,
                message = "Term name must not exceed 100 characters."
        )
        String termName,

        @NotNull(
                message = "Term start date is required."
        )
        LocalDate startDate,

        @NotNull(
                message = "Term end date is required."
        )
        LocalDate endDate,

        @NotNull(
                message = "Display order is required."
        )
        @Positive(
                message = "Display order must be greater than zero."
        )
        Integer displayOrder,

        @NotNull(
                message = "Term status is required."
        )
        ErpAcademicTerm.Status status,

        Boolean currentTerm,

        @Size(
                max = 500,
                message = "Description must not exceed 500 characters."
        )
        String description,

        Boolean active,

        Long version
) {

    @AssertTrue(
            message = "Term end date cannot be before start date."
    )
    public boolean isTermDateRangeValid() {
        return startDate == null
                || endDate == null
                || !endDate.isBefore(startDate);
    }
}