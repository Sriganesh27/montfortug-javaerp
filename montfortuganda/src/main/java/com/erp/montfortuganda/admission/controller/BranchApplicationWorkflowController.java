package com.erp.montfortuganda.admission.controller;

import com.erp.montfortuganda.admission.dto.ApplicationInterviewCompleteRequestDTO;
import com.erp.montfortuganda.admission.dto.ApplicationInterviewResponseDTO;
import com.erp.montfortuganda.admission.dto.ApplicationInterviewScheduleRequestDTO;
import com.erp.montfortuganda.admission.dto.ApplicationInterviewWaitlistResultRequestDTO;
import com.erp.montfortuganda.admission.dto.ApplicationStageTransitionRequestDTO;
import com.erp.montfortuganda.admission.dto.ApplicationStageTransitionResponseDTO;
import com.erp.montfortuganda.admission.dto.ApplicationStageTransitionResponseDTO.AvailableTransition;
import com.erp.montfortuganda.admission.service.ApplicationInterviewService;
import com.erp.montfortuganda.admission.service.ApplicationStageTransitionService;
import com.erp.montfortuganda.auth.service.CurrentUserContext;
import com.erp.montfortuganda.auth.service.CurrentUserService;
import com.erp.montfortuganda.dto.ApiResponse;
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

    private final CurrentUserService currentUserService;

    public BranchApplicationWorkflowController(
            ApplicationStageTransitionService transitionService,
            ApplicationInterviewService interviewService,
            CurrentUserService currentUserService
    ) {
        this.transitionService = transitionService;
        this.interviewService = interviewService;
        this.currentUserService = currentUserService;
    }

    /**
     * Returns the backend-approved actions for the application's currently
     * saved workflow stage.
     */
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

    /**
     * Applies one validated and auditable application workflow transition.
     */
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

    /**
     * Returns the current Entrance Test state for the application.
     */
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

    /**
     * Schedules the Entrance Test and internally assigns the responsible
     * branch employee. Employee assignment itself does not trigger a parent
     * email.
     */
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

    /**
     * Reschedules an existing Entrance Test.
     */
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

    /**
     * Marks the scheduled Entrance Test as IN_PROGRESS.
     */
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
     * Finalizes a completed WAITLIST Entrance Test without changing its
     * subject marks, employee assignment or completion time.
     *
     * <p>Only WAITLIST -> PASSED and WAITLIST -> FAILED are accepted.</p>
     */
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

    /**
     * Saves subject-wise marks, calculates totals/percentage in the backend,
     * and records the final Entrance Test result.
     */
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

}
