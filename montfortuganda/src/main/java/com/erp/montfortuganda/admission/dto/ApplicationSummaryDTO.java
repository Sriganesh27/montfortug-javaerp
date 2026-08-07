package com.erp.montfortuganda.admission.dto;

import com.erp.montfortuganda.admission.dto.ApplicationStageTransitionRequestDTO.TransitionAction;
import com.erp.montfortuganda.admission.entity.ErpApplication;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Compact branch-facing admission application list row.
 *
 * <p>This DTO contains the authoritative workflow and document states needed
 * by the Applications list. It also carries one backend-approved primary
 * action so the browser does not need to open every applicant profile merely
 * to discover the next step.</p>
 */
@Data
public class ApplicationSummaryDTO {

    // ---------------------------------------------------------------------
    // Application identity
    // ---------------------------------------------------------------------

    private Long applicationId;
    private String applicationNo;
    private String studentName;

    /**
     * Student gender used by the branch list and Gender filter.
     */
    private ErpApplication.Gender gender;

    /**
     * Resolved academic level name, for example Nursery or Primary.
     */
    private String levelName;

    /**
     * Resolved class name, for example Baby Class or Primary 2.
     */
    private String className;

    // ---------------------------------------------------------------------
    // Status values displayed in the list
    // ---------------------------------------------------------------------

    /**
     * Existing compatibility field used by the current applications.js.
     * It contains the same value as applicationStatus.name().
     */
    private String status;

    private ErpApplication.ApplicationStatus applicationStatus;
    private ErpApplication.CurrentStage currentStage;
    private ErpApplication.DocumentStatus documentStatus;
    private ErpApplication.VerificationStatus verificationStatus;

    /**
     * Kept as String because ErpApplication.scholarshipStatus currently
     * supports legacy database values.
     */
    private String scholarshipStatus;

    private ErpApplication.AdmissionStatus admissionStatus;

    private Boolean workflowLocked;

    // ---------------------------------------------------------------------
    // Primary list action
    // ---------------------------------------------------------------------

    /**
     * First backend-approved non-destructive transition for the current stage.
     * Reject, close, return and reopen remain inside the full profile.
     */
    private TransitionAction nextAction;

    private ErpApplication.CurrentStage nextTargetStage;

    /**
     * Human-readable action label, for example:
     * "Start verification", "Move to school visit", or "Open profile".
     */
    private String nextActionLabel;

    /**
     * Indicates whether the list may show the primary action button.
     */
    private Boolean nextActionAvailable;

    // ---------------------------------------------------------------------
    // Dates used for sorting and display
    // ---------------------------------------------------------------------

    private LocalDateTime submittedDate;
    private LocalDateTime updatedAt;
}
