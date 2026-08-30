package com.erp.montfortuganda.admission.controller;

import com.erp.montfortuganda.admission.dto.ApplicationInterviewCompleteRequestDTO;
import com.erp.montfortuganda.admission.dto.ApplicationInterviewScheduleRequestDTO;
import com.erp.montfortuganda.admission.dto.ApplicationInterviewWaitlistRequestDTO;
import com.erp.montfortuganda.admission.dto.ApplicationInterviewWaitlistResultRequestDTO;
import com.erp.montfortuganda.admission.dto.ApplicationFeeDiscussionRequestDTO;
import com.erp.montfortuganda.admission.dto.ApplicationFeeDiscussionResponseDTO;
import com.erp.montfortuganda.admission.dto.ApplicationStageTransitionRequestDTO;
import com.erp.montfortuganda.admission.dto.ApplicationStageTransitionResponseDTO;
import com.erp.montfortuganda.admission.dto.ApplicationStageTransitionResponseDTO.AvailableTransition;
import com.erp.montfortuganda.admission.service.ApplicationInterviewService;
import com.erp.montfortuganda.admission.service.ApplicationFeeDiscussionService;
import com.erp.montfortuganda.admission.service.ApplicationStageTransitionService;
import com.erp.montfortuganda.auth.service.CurrentUserContext;
import com.erp.montfortuganda.auth.service.CurrentUserService;
import com.erp.montfortuganda.dto.ApiResponse;
import com.erp.montfortuganda.scholarship.dto.ScholarshipApplicationFormRequestDTO;
import com.erp.montfortuganda.scholarship.dto.ScholarshipApplicationFormResponseDTO;
import com.erp.montfortuganda.scholarship.service.ScholarshipService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.erp.montfortuganda.admission.dto.ApplicationInterviewResponseDTO;

import java.util.List;

/**
 * Secure branch-admin admission workflow endpoints.
 *
 * <p>The browser never supplies a branch ID. The authenticated user's branch
 * is resolved by {@link CurrentUserService}, while the service independently
 * verifies application ownership and locks the application before mutation.</p>
 */
@RestController
@RequestMapping(
        "/api/admission/branch/applications/{applicationId}/workflow"
)
@PreAuthorize("hasRole('BRANCH_ADMIN')")
public class BranchApplicationWorkflowController {

    private final ApplicationStageTransitionService
            transitionService;

    private final ApplicationInterviewService
            interviewService;

    private final ApplicationFeeDiscussionService
            feeDiscussionService;

    private final ScholarshipService
            scholarshipService;

    private final CurrentUserService currentUserService;

    public BranchApplicationWorkflowController(
            ApplicationStageTransitionService transitionService,
            ApplicationInterviewService interviewService,
            ApplicationFeeDiscussionService feeDiscussionService,
            ScholarshipService scholarshipService,
            CurrentUserService currentUserService
    ) {
        this.transitionService = transitionService;
        this.interviewService = interviewService;
        this.feeDiscussionService = feeDiscussionService;
        this.scholarshipService = scholarshipService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/transitions")
    public ResponseEntity<
            ApiResponse<List<AvailableTransition>>>
    getAvailableTransitions(
            Authentication authentication,
            @PathVariable Long applicationId
    ) {
        CurrentUserContext context =
                currentUserService.getCurrentUserContext(
                        authentication
                );

        List<AvailableTransition> transitions =
                transitionService.getAvailableTransitions(
                        context,
                        applicationId
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Available application workflow transitions "
                                + "fetched successfully",
                        transitions
                )
        );
    }

    @PatchMapping("/transition")
    public ResponseEntity<
            ApiResponse<ApplicationStageTransitionResponseDTO>>
    transitionApplication(
            Authentication authentication,
            @PathVariable Long applicationId,
            @Valid @RequestBody
            ApplicationStageTransitionRequestDTO request
    ) {
        CurrentUserContext context =
                currentUserService.getCurrentUserContext(
                        authentication
                );

        ApplicationStageTransitionResponseDTO response =
                transitionService.transition(
                        context,
                        applicationId,
                        request
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Application workflow updated successfully",
                        response
                )
        );
    }

    @GetMapping("/entrance-test")
    public ResponseEntity<
            ApiResponse<ApplicationInterviewResponseDTO>>
    getEntranceTest(
            Authentication authentication,
            @PathVariable Long applicationId
    ) {
        CurrentUserContext context =
                currentUserService.getCurrentUserContext(
                        authentication
                );

        ApplicationInterviewResponseDTO response =
                interviewService.getInterview(
                        context,
                        applicationId
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Entrance Test details fetched successfully",
                        response
                )
        );
    }

    @PostMapping("/entrance-test/schedule")
    public ResponseEntity<
            ApiResponse<ApplicationInterviewResponseDTO>>
    scheduleEntranceTest(
            Authentication authentication,
            @PathVariable Long applicationId,
            @Valid @RequestBody
            ApplicationInterviewScheduleRequestDTO request
    ) {
        CurrentUserContext context =
                currentUserService.getCurrentUserContext(
                        authentication
                );

        ApplicationInterviewResponseDTO response =
                interviewService.scheduleInterview(
                        context,
                        applicationId,
                        request
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Entrance Test scheduled successfully",
                        response
                )
        );
    }

    @PatchMapping("/entrance-test/schedule")
    public ResponseEntity<
            ApiResponse<ApplicationInterviewResponseDTO>>
    rescheduleEntranceTest(
            Authentication authentication,
            @PathVariable Long applicationId,
            @Valid @RequestBody
            ApplicationInterviewScheduleRequestDTO request
    ) {
        CurrentUserContext context =
                currentUserService.getCurrentUserContext(
                        authentication
                );

        ApplicationInterviewResponseDTO response =
                interviewService.rescheduleInterview(
                        context,
                        applicationId,
                        request
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Entrance Test rescheduled successfully",
                        response
                )
        );
    }

    @PatchMapping("/entrance-test/start")
    public ResponseEntity<
            ApiResponse<ApplicationInterviewResponseDTO>>
    startEntranceTest(
            Authentication authentication,
            @PathVariable Long applicationId
    ) {
        CurrentUserContext context =
                currentUserService.getCurrentUserContext(
                        authentication
                );

        ApplicationInterviewResponseDTO response =
                interviewService.startInterview(
                        context,
                        applicationId
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Entrance Test started successfully",
                        response
                )
        );
    }

    /**
     * Places or releases an application on/from the application-level
     * admission waitlist without changing the recorded Entrance Test result,
     * marks, or attempt history. Branch ownership is enforced by the service.
     */
    @PatchMapping("/entrance-test/waitlist")
    public ResponseEntity<
            ApiResponse<ApplicationInterviewResponseDTO>>
    updateEntranceTestWaitlist(
            Authentication authentication,
            @PathVariable Long applicationId,
            @Valid @RequestBody
            ApplicationInterviewWaitlistRequestDTO request
    ) {
        CurrentUserContext context =
                currentUserService.getCurrentUserContext(
                        authentication
                );

        ApplicationInterviewResponseDTO response =
                interviewService.updateApplicationWaitlist(
                        context,
                        applicationId,
                        request
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        Boolean.TRUE.equals(request.waitlisted())
                                ? "Application placed on waitlist successfully"
                                : "Application released from waitlist successfully",
                        response
                )
        );
    }

    @PatchMapping("/entrance-test/waitlist-result")
    public ResponseEntity<
            ApiResponse<ApplicationInterviewResponseDTO>>
    updateEntranceTestWaitlistResult(
            Authentication authentication,
            @PathVariable Long applicationId,
            @Valid @RequestBody
            ApplicationInterviewWaitlistResultRequestDTO request
    ) {
        CurrentUserContext context =
                currentUserService.getCurrentUserContext(
                        authentication
                );

        ApplicationInterviewResponseDTO response =
                interviewService.updateWaitlistResult(
                        context,
                        applicationId,
                        request
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Entrance Test waitlist decision updated successfully",
                        response
                )
        );
    }

    @PatchMapping("/entrance-test/complete")
    public ResponseEntity<
            ApiResponse<ApplicationInterviewResponseDTO>>
    completeEntranceTest(
            Authentication authentication,
            @PathVariable Long applicationId,
            @Valid @RequestBody
            ApplicationInterviewCompleteRequestDTO request
    ) {
        CurrentUserContext context =
                currentUserService.getCurrentUserContext(
                        authentication
                );

        ApplicationInterviewResponseDTO response =
                interviewService.completeInterview(
                        context,
                        applicationId,
                        request
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Entrance Test completed successfully",
                        response
                )
        );
    }

    @GetMapping("/fee-discussion")
    public ResponseEntity<
            ApiResponse<ApplicationFeeDiscussionResponseDTO>>
    getFeeDiscussion(
            Authentication authentication,
            @PathVariable Long applicationId
    ) {
        CurrentUserContext context =
                currentUserService.getCurrentUserContext(
                        authentication
                );

        ApplicationFeeDiscussionResponseDTO response =
                feeDiscussionService.getFeeDiscussion(
                        context,
                        applicationId
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Fee discussion details fetched successfully",
                        response
                )
        );
    }

    @PatchMapping("/fee-discussion")
    public ResponseEntity<
            ApiResponse<ApplicationFeeDiscussionResponseDTO>>
    saveFeeDiscussion(
            Authentication authentication,
            @PathVariable Long applicationId,
            @Valid @RequestBody
            ApplicationFeeDiscussionRequestDTO request
    ) {
        CurrentUserContext context =
                currentUserService.getCurrentUserContext(
                        authentication
                );

        ApplicationFeeDiscussionResponseDTO response =
                feeDiscussionService.saveFeeDiscussion(
                        context,
                        applicationId,
                        request
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Fee discussion saved successfully",
                        response
                )
        );
    }

    @PatchMapping("/fee-discussion/finalize")
    public ResponseEntity<
            ApiResponse<ApplicationStageTransitionResponseDTO>>
    finalizeFeeDiscussion(
            Authentication authentication,
            @PathVariable Long applicationId
    ) {
        CurrentUserContext context =
                currentUserService.getCurrentUserContext(
                        authentication
                );

        ApplicationStageTransitionResponseDTO response =
                feeDiscussionService.finalizeFeeDiscussion(
                        context,
                        applicationId
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Fee discussion finalized successfully",
                        response
                )
        );
    }

    /**
     * Loads the existing scholarship application form for the branch.
     *
     * <p>Application/student/fee identity is resolved server-side. The same
     * scholarship record is later used by the public-token route as well.</p>
     */
    @GetMapping("/scholarship/application-form")
    public ResponseEntity<
            ApiResponse<ScholarshipApplicationFormResponseDTO>>
    getScholarshipApplicationForm(
            Authentication authentication,
            @PathVariable Long applicationId
    ) {
        currentUserService.getCurrentUserContext(
                authentication
        );

        ScholarshipApplicationFormResponseDTO response =
                scholarshipService.getApplicationFormForBranch(
                        applicationId
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Scholarship application form fetched successfully",
                        response
                )
        );
    }

    /**
     * Saves a school-assisted scholarship application as a draft/in-progress
     * form without submitting it for review.
     */
    @PatchMapping("/scholarship/application-form")
    public ResponseEntity<
            ApiResponse<ScholarshipApplicationFormResponseDTO>>
    saveScholarshipApplicationForm(
            Authentication authentication,
            @PathVariable Long applicationId,
            @RequestBody
            ScholarshipApplicationFormRequestDTO request
    ) {
        currentUserService.getCurrentUserContext(
                authentication
        );

        ScholarshipApplicationFormResponseDTO response =
                scholarshipService.saveApplicationFormForBranch(
                        applicationId,
                        request
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Scholarship application draft saved successfully",
                        response
                )
        );
    }

    /**
     * Final submission of the school-assisted scholarship application.
     */
    @PostMapping("/scholarship/application-form/submit")
    public ResponseEntity<
            ApiResponse<ScholarshipApplicationFormResponseDTO>>
    submitScholarshipApplicationForm(
            Authentication authentication,
            @PathVariable Long applicationId,
            @RequestBody
            ScholarshipApplicationFormRequestDTO request
    ) {
        currentUserService.getCurrentUserContext(
                authentication
        );

        ScholarshipApplicationFormResponseDTO response =
                scholarshipService.submitApplicationFormForBranch(
                        applicationId,
                        request
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Scholarship application submitted successfully",
                        response
                )
        );
    }

    /**
     * Issues/reissues a short-lived opaque access key for the authenticated
     * school-assisted Scholarship Application.
     *
     * <p>The admission application must already be in the SCHOLARSHIP stage.
     * The raw key is returned only once to the authenticated Branch Admin
     * browser so it can open the school-assisted Scholarship form. No
     * application, scholarship, branch, student, or employee ID is placed in
     * the Scholarship form URL.</p>
     */
    @PostMapping("/scholarship/school-access")
    public ResponseEntity<
            ApiResponse<ScholarshipService.SchoolScholarshipAccessKey>>
    issueSchoolScholarshipAccess(
            Authentication authentication,
            @PathVariable Long applicationId
    ) {
        /*
         * Resolve authenticated branch context before issuing the key.
         * ScholarshipService independently verifies branch ownership again.
         */
        currentUserService.getCurrentUserContext(
                authentication
        );

        ScholarshipService.SchoolScholarshipAccessKey response =
                scholarshipService.issueSchoolApplicationAccess(
                        applicationId
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "School-assisted scholarship access generated successfully",
                        response
                )
        );
    }

    /**
     * Issues/reissues the secure public scholarship application link.
     *
     * <p>The Scholarship service generates the raw public token internally,
     * stores only its SHA-256 hash, and passes the raw token only to the
     * after-commit email event. The authenticated browser receives only safe
     * link status/expiry metadata and never receives the raw parent token.</p>
     */
    @PostMapping("/scholarship/application-link")
    public ResponseEntity<
            ApiResponse<ScholarshipService.PublicScholarshipLinkToken>>
    issueScholarshipApplicationLink(
            Authentication authentication,
            @PathVariable Long applicationId
    ) {
        /*
         * Forces authentication resolution here as well as in the scholarship
         * service. The browser still never supplies a branch ID.
         */
        currentUserService.getCurrentUserContext(
                authentication
        );

        ScholarshipService.PublicScholarshipLinkToken response =
                scholarshipService.issuePublicApplicationToken(
                        applicationId
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Scholarship application link generated successfully",
                        response
                )
        );
    }
}
