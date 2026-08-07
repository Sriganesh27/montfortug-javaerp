package com.erp.montfortuganda.admission.dto;

import com.erp.montfortuganda.admission.dto.ApplicationStageTransitionRequestDTO.TransitionAction;
import com.erp.montfortuganda.admission.entity.ErpApplication;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Branch-facing result returned after an admission workflow transition.
 *
 * <p>The response contains the authoritative stage and related workflow
 * statuses saved by the backend. It also returns the newly created history
 * entry and the transitions that are currently available, allowing the
 * application profile to update without reloading the complete applicant
 * record.</p>
 */
@Data
public class ApplicationStageTransitionResponseDTO {

    // ---------------------------------------------------------------------
    // Application identity
    // ---------------------------------------------------------------------

    private Long applicationId;
    private String applicationNo;

    // ---------------------------------------------------------------------
    // Applied transition
    // ---------------------------------------------------------------------

    private ErpApplication.CurrentStage previousStage;
    private ErpApplication.CurrentStage currentStage;
    private TransitionAction action;

    private String publicRemarks;
    private String internalRemarks;

    // ---------------------------------------------------------------------
    // Authoritative workflow state after the transition
    // ---------------------------------------------------------------------

    private ErpApplication.ApplicationStatus applicationStatus;
    private ErpApplication.VerificationStatus verificationStatus;
    private ErpApplication.DocumentStatus documentStatus;
    private ErpApplication.TestStatus testStatus;
    private ErpApplication.FeeDecisionStatus feeDecisionStatus;
    private String scholarshipWorkflowStatus;
    private ErpApplication.PaymentStatus paymentStatus;
    private ErpApplication.AdmissionStatus admissionStatus;

    private Boolean workflowLocked;

    // ---------------------------------------------------------------------
    // Audit/history result
    // ---------------------------------------------------------------------

    private Long historyId;
    private String historyStage;
    private String transitionSource;

    private Long changedBy;
    private LocalDateTime changedAt;

    private Boolean emailRequired;
    private String emailStatus;
    private String emailType;
    private LocalDateTime emailSentAt;

    // ---------------------------------------------------------------------
    // UI continuation data
    // ---------------------------------------------------------------------

    /**
     * Valid transitions from the newly saved current stage. These values are
     * informational for rendering branch-admin actions; the backend still
     * validates every submitted request independently.
     */
    private List<AvailableTransition> availableTransitions =
            new ArrayList<>();

    /**
     * One backend-approved action that may be rendered by the branch
     * application profile.
     */
    @Data
    public static class AvailableTransition {

        private TransitionAction action;
        private ErpApplication.CurrentStage targetStage;

        /**
         * Human-readable button/menu label supplied by the backend.
         */
        private String label;

        /**
         * Indicates that the branch administrator may request an applicant
         * email for this transition.
         */
        private Boolean applicantNotificationSupported;

        /**
         * Indicates that applicant notification must be sent for this
         * transition regardless of the browser checkbox.
         */
        private Boolean applicantNotificationRequired;

        /**
         * Indicates whether public remarks should be collected before the
         * action is submitted.
         */
        private Boolean publicRemarksSupported;

        /**
         * Indicates whether internal remarks should be collected before the
         * action is submitted.
         */
        private Boolean internalRemarksSupported;

        /**
         * Indicates whether either public or internal remarks are mandatory.
         */
        private Boolean remarksRequired;
    }
}
