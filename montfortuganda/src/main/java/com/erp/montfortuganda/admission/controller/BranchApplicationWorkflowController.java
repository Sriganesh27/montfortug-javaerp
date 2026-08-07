package com.erp.montfortuganda.admission.controller;

import com.erp.montfortuganda.admission.dto.ApplicationStageTransitionRequestDTO;
import com.erp.montfortuganda.admission.dto.ApplicationStageTransitionResponseDTO;
import com.erp.montfortuganda.admission.dto.ApplicationStageTransitionResponseDTO.AvailableTransition;
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

    private final CurrentUserService currentUserService;

    public BranchApplicationWorkflowController(
            ApplicationStageTransitionService transitionService,
            CurrentUserService currentUserService
    ) {
        this.transitionService = transitionService;
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
}
