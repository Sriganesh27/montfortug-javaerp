package com.erp.montfortuganda.school.dto;

import com.erp.montfortuganda.school.entity.ErpClassSubject;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ClassSubjectRequest(
        @NotNull(message = "Academic year is required") @Positive Long academicYearId,
        @NotNull(message = "Level is required") @Positive Integer levelId,
        @NotNull(message = "Subject is required") @Positive Long subjectId,
        @NotNull(message = "Starting class is required") @Positive Integer startClassId,
        @Positive Integer endClassId,
        @NotNull(message = "Requirement is required") ErpClassSubject.Requirement subjectRequirement
) {}
