package com.erp.montfortuganda.school.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/** Request used to create or update a branch-owned class. */
public record SchoolClassRequest(
        @NotNull(message = "Academic year is required.")
        @Positive(message = "Academic year ID must be greater than zero.")
        Long academicYearId,

        @NotNull(message = "Level is required.")
        @Positive(message = "Level ID must be greater than zero.")
        Integer levelId,

        @NotBlank(message = "Class code is required.")
        @Size(max = 255, message = "Class code cannot exceed 255 characters.")
        String classCode,

        @NotBlank(message = "Class name is required.")
        @Size(max = 255, message = "Class name cannot exceed 255 characters.")
        String className,


        @NotNull(message = "Status is required.")
        @PositiveOrZero(message = "Status must be zero or one.")
        Integer status
) {}
