package com.erp.montfortuganda.student.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Request payload for assigning or changing an enrolled student's A' Level
 * combination.
 *
 * Student ID is supplied in the endpoint path. Branch ownership is resolved
 * from the authenticated user and is not accepted from the client.
 */
public record ErpStudentALevelSelectionRequest(

        @NotNull(message = "Academic year is required.")
        @Positive(message = "Academic year ID must be greater than zero.")
        Long academicYearId,

        @NotNull(message = "Class is required.")
        @Positive(message = "Class ID must be greater than zero.")
        Integer classId,

        @NotNull(message = "A' Level combination is required.")
        @Positive(message = "Combination ID must be greater than zero.")
        Long combinationId
) {
}
