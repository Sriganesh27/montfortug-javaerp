package com.erp.montfortuganda.admission.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Branch Admin request for scheduling or rescheduling a School Visit.
 *
 * <p>This request intentionally does not contain an employee ID.</p>
 *
 * <p>The School Visit is first scheduled after document verification.
 * The responsible employee is assigned separately only when the parent/student
 * attends the school visit.</p>
 *
 * <p>The backend resolves the Branch from the authenticated user.
 * No Branch ID is accepted from the browser.</p>
 */
@Data
public class ApplicationSchoolVisitScheduleRequestDTO {

    /**
     * Planned School Visit date and time.
     */
    @NotNull(message = "School visit date and time are required.")
    @FutureOrPresent(
            message = "School visit date and time cannot be in the past."
    )
    private LocalDateTime scheduledAt;

    /**
     * Optional internal scheduling/rescheduling remarks.
     */
    @Size(
            max = 1000,
            message = "School visit remarks cannot exceed 1000 characters."
    )
    private String remarks;
}
