package com.erp.montfortuganda.admission.dto;

import com.erp.montfortuganda.admission.entity.ErpApplicationInterview;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Completes an Entrance Test with subject-wise marks and a final result.
 *
 * <p>Overall maximum marks, obtained marks and percentage are calculated by
 * the backend from the submitted subject rows and are not accepted as separate
 * client-entered totals.</p>
 */
public record ApplicationInterviewCompleteRequestDTO(

        @PastOrPresent
        LocalDateTime completedAt,

        @NotEmpty
        List<@Valid ApplicationInterviewMarkRequestDTO> marks,

        @NotNull
        ErpApplicationInterview.Result result,

        @Size(max = 5000)
        String employeeRemarks,

        @Size(max = 5000)
        String internalRemarks
) {
}
