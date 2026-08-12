package com.erp.montfortuganda.admission.dto;

import com.erp.montfortuganda.admission.entity.ErpApplication;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Branch-admin request for moving an admission application between workflow
 * stages.
 *
 * <p>The service validates the requested action and target stage against the
 * application state loaded under a pessimistic database lock. The client
 * cannot use this DTO to bypass, skip, or reorder admission stages.</p>
 *
 * <p>For the specific transition from APPLICATION_VERIFICATION to SCHOOL_VISIT,
 * the browser must also submit the planned School Visit date/time. This allows
 * the stage transition and visit scheduling to be committed atomically in one
 * transaction.</p>
 */
@Data
public class ApplicationStageTransitionRequestDTO {

    /**
     * Stage shown to the branch administrator when the action was submitted.
     * The backend compares this value with the locked database record to
     * reject stale or duplicate transition requests.
     */
    @NotNull(message = "Expected current stage is required")
    private ErpApplication.CurrentStage expectedCurrentStage;

    /**
     * Stage requested by the branch administrator. The workflow validator
     * decides whether this target is permitted for the supplied action.
     */
    @NotNull(message = "Target stage is required")
    private ErpApplication.CurrentStage targetStage;

    /**
     * Business meaning of the requested transition.
     */
    @NotNull(message = "Workflow action is required")
    private TransitionAction action;

    /**
     * Required only when advancing from APPLICATION_VERIFICATION to
     * SCHOOL_VISIT.
     *
     * <p>Employee assignment is intentionally not part of this request.
     * The responsible employee is assigned later when the parent/student
     * actually attends the visit.</p>
     */
    @FutureOrPresent(
            message = "School visit date and time cannot be in the past"
    )
    private LocalDateTime schoolVisitScheduledAt;

    /**
     * Applicant-visible explanation. This value may be included in email or
     * portal notifications and therefore must not contain internal notes.
     */
    @Size(
            max = 1000,
            message = "Public remarks must not exceed 1000 characters"
    )
    private String publicRemarks;

    /**
     * School-only remarks recorded in application status history.
     */
    @Size(
            max = 2000,
            message = "Internal remarks must not exceed 2000 characters"
    )
    private String internalRemarks;

    /**
     * Requests an applicant notification after the transaction commits.
     * The backend remains responsible for deciding whether the action supports
     * or requires a notification and for selecting the email template.
     */
    private Boolean notifyApplicant = false;

    /**
     * Generic workflow commands. Allowed stage/action combinations are
     * enforced centrally by the workflow validator and never trusted from the
     * browser.
     */
    public enum TransitionAction {
        ADVANCE,
        RETURN,
        REJECT,
        CLOSE,
        REOPEN
    }
}
