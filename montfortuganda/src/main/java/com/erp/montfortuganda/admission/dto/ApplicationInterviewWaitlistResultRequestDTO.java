package com.erp.montfortuganda.admission.dto;

import com.erp.montfortuganda.admission.entity.ErpApplicationInterview;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Final decision for a completed Entrance Test currently placed on WAITLIST.
 *
 * <p>The service only accepts PASSED or FAILED. Existing subject marks,
 * employee assignment and completion timestamps are not edited by this action.</p>
 */
public record ApplicationInterviewWaitlistResultRequestDTO(

        @NotNull
        ErpApplicationInterview.Result result,

        @NotBlank
        @Size(max = 2000)
        String remarks
) {
}
