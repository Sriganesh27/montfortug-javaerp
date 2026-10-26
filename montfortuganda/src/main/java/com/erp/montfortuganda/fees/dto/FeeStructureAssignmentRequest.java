package com.erp.montfortuganda.fees.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

/** Request to assign a published structure to one or more enrolled students. */
public record FeeStructureAssignmentRequest(
        @NotEmpty(message = "Select at least one student.")
        List<@NotNull @Positive Long> studentIds
) {
}
