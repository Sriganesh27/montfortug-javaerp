package com.erp.montfortuganda.student.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

/** The server derives branch/year/class/student from the enrollment; clients cannot set them. */
public record StudentSubjectSelectionRequest(
        @NotNull(message = "Selected subject IDs are required")
        @Size(min = 1, max = 20, message = "Select the required number of optional subjects")
        List<@NotNull(message = "Subject ID cannot be empty") @Positive(message = "Subject ID must be greater than zero") Long> subjectIds
) {}
