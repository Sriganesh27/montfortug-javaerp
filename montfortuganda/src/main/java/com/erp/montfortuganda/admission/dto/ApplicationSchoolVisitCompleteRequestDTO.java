package com.erp.montfortuganda.admission.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Branch Admin request used when the parent/student actually attends the
 * scheduled School Visit and the application is ready to proceed to the
 * Entrance Test process.
 *
 * <p>This existing DTO is reused for the visit-day action. It now carries the
 * responsible employee together with attendance, actual visit time and
 * remarks. The backend validates that the employee is active and belongs to
 * the authenticated Branch.</p>
 *
 * <p>This action does not send a parent email.</p>
 */
@Data
public class ApplicationSchoolVisitCompleteRequestDTO {

    /**
     * Responsible employee handling the visit/test process.
     */
    @NotNull(message = "Responsible employee is required.")
    @Positive(message = "Responsible employee ID must be valid.")
    private Long employeeId;

    /**
     * Actual date and time when the parent/student attended the school.
     *
     * <p>If supplied, it cannot be in the future. The service may use the
     * current server time when this value is omitted.</p>
     */
    @PastOrPresent(
            message = "School visit time cannot be in the future."
    )
    private LocalDateTime visitedAt;

    /**
     * Whether the student attended the school visit.
     */
    @NotNull(message = "Student attendance selection is required.")
    private Boolean studentAttended;

    /**
     * Whether a parent or guardian attended the school visit.
     */
    @NotNull(message = "Parent or guardian attendance selection is required.")
    private Boolean parentAttended;

    /**
     * School Visit remarks recorded before proceeding to Entrance Test.
     */
    @Size(
            max = 1000,
            message = "School visit remarks cannot exceed 1000 characters."
    )
    private String remarks;
}
