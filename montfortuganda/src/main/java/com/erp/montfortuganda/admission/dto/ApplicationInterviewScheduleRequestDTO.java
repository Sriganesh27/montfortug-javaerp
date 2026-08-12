package com.erp.montfortuganda.admission.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * Request used to schedule or reschedule an Entrance Test.
 */
public record ApplicationInterviewScheduleRequestDTO(

        @NotNull
        Long employeeId,

        @NotNull
        @FutureOrPresent
        LocalDateTime scheduledAt,

        @Size(max = 5000)
        String employeeRemarks,

        @Size(max = 5000)
        String internalRemarks
) {
}
