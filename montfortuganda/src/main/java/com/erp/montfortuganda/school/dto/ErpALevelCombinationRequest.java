package com.erp.montfortuganda.school.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Request payload for creating or updating an A' Level combination.
 *
 * The branch is intentionally omitted. The service derives the branch from
 * the authenticated user and does not trust a branch ID supplied by a client.
 */
public record ErpALevelCombinationRequest(

        @NotBlank(message = "Combination code is required.")
        @Size(max = 30, message = "Combination code cannot exceed 30 characters.")
        String combinationCode,

        @NotBlank(message = "Combination name is required.")
        @Size(max = 150, message = "Combination name cannot exceed 150 characters.")
        String combinationName,

        @NotNull(message = "Principal subject 1 is required.")
        @Positive(message = "Principal subject 1 ID must be greater than zero.")
        Long principalSubject1Id,

        @NotNull(message = "Principal subject 2 is required.")
        @Positive(message = "Principal subject 2 ID must be greater than zero.")
        Long principalSubject2Id,

        @NotNull(message = "Principal subject 3 is required.")
        @Positive(message = "Principal subject 3 ID must be greater than zero.")
        Long principalSubject3Id,

        @NotNull(message = "Subsidiary subject is required.")
        @Positive(message = "Subsidiary subject ID must be greater than zero.")
        Long subsidiarySubjectId
) {
}
