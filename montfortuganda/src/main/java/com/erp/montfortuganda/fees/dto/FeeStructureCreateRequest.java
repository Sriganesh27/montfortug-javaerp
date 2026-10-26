package com.erp.montfortuganda.fees.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record FeeStructureCreateRequest(
        @NotNull @Positive Long academicYearId,
        @NotNull @Positive Integer classId,
        @NotNull @Positive Long termId,
        @Size(max = 500) String remarks
) {
}
