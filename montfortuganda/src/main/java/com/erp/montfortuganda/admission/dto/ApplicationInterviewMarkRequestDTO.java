package com.erp.montfortuganda.admission.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * One subject-wise Entrance Test mark row.
 *
 * <p>The backend validates that the subject exists and is active, prevents
 * duplicate subjects for the same interview, and calculates the percentage
 * from obtainedMarks / maximumMarks.</p>
 */
public record ApplicationInterviewMarkRequestDTO(

        @NotNull
        Long subjectId,

        @NotNull
        @DecimalMin(value = "0.01")
        @Digits(integer = 5, fraction = 2)
        BigDecimal maximumMarks,

        @NotNull
        @DecimalMin(value = "0.00")
        @Digits(integer = 5, fraction = 2)
        BigDecimal obtainedMarks,

        @Size(max = 500)
        String remarks
) {
}
