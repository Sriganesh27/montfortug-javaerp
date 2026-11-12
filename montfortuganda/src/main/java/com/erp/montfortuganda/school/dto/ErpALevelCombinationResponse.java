package com.erp.montfortuganda.school.dto;

import java.time.LocalDateTime;

/**
 * Response payload for an A' Level combination.
 *
 * Subject details are flattened so the frontend does not need to interpret
 * nested JPA entities.
 */
public record ErpALevelCombinationResponse(

        Long combinationId,
        Integer branchId,
        String combinationCode,
        String combinationName,

        Long principalSubject1Id,
        String principalSubject1Code,
        String principalSubject1Name,

        Long principalSubject2Id,
        String principalSubject2Code,
        String principalSubject2Name,

        Long principalSubject3Id,
        String principalSubject3Code,
        String principalSubject3Name,

        Long subsidiarySubjectId,
        String subsidiarySubjectCode,
        String subsidiarySubjectName,

        Boolean active,
        Long version,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
