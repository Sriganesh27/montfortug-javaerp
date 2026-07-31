package com.erp.montfortuganda.school.dto;

import com.erp.montfortuganda.school.entity.ErpSection;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/**
 * Request model for creating or updating a branch-owned Section.
 *
 * <p>Branch ID is intentionally excluded. The service must always use the
 * authenticated user's branch and verify that the selected Academic Year
 * belongs to that same branch.</p>
 */
public record SectionRequest(

        @NotNull(
                message = "Academic Year is required."
        )
        @Positive(
                message = "Academic Year ID must be greater than zero."
        )
        Long academicYearId,

        @NotNull(
                message = "Class is required."
        )
        @Positive(
                message = "Class ID must be greater than zero."
        )
        Integer classId,

        @NotBlank(
                message = "Section code is required."
        )
        @Size(
                max = 20,
                message = "Section code must not exceed 20 characters."
        )
        String sectionCode,

        @NotBlank(
                message = "Section name is required."
        )
        @Size(
                max = 100,
                message = "Section name must not exceed 100 characters."
        )
        String sectionName,

        @NotNull(
                message = "Section capacity is required."
        )
        @Positive(
                message = "Section capacity must be greater than zero."
        )
        Integer capacity,

        @Size(
                max = 500,
                message = "Description must not exceed 500 characters."
        )
        String description,

        @NotNull(
                message = "Section status is required."
        )
        ErpSection.Status status,

        Boolean active,

        @PositiveOrZero(
                message = "Section version cannot be negative."
        )
        Long version
) {
}