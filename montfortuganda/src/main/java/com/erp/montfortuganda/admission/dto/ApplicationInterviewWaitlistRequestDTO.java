package com.erp.montfortuganda.admission.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Places an application on, or releases it from, the admission waitlist
 * without changing the recorded Entrance Test result or marks.
 */
public record ApplicationInterviewWaitlistRequestDTO(

        @NotNull
        Boolean waitlisted,

        @NotBlank
        @Size(max = 2000)
        String remarks
) {
}
