package com.erp.montfortuganda.admission.controller;

import com.erp.montfortuganda.admission.dto.ApplicationSummaryDTO;
import com.erp.montfortuganda.admission.dto.BranchApplicationDetailsResponseDTO;
import com.erp.montfortuganda.admission.service.BranchAdmissionService;
import com.erp.montfortuganda.auth.service.CurrentUserContext;
import com.erp.montfortuganda.auth.service.CurrentUserService;
import com.erp.montfortuganda.dto.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Branch-scoped admission application read operations.
 *
 * <p>The authenticated user's branch is resolved on the server. No branch ID
 * is accepted from the browser.</p>
 */
@RestController
@RequestMapping("/api/admission/branch")
@PreAuthorize("hasRole('BRANCH_ADMIN')")
public class BranchAdmissionController {

    private final BranchAdmissionService admissionService;
    private final CurrentUserService currentUserService;

    public BranchAdmissionController(
            BranchAdmissionService admissionService,
            CurrentUserService currentUserService
    ) {
        this.admissionService = admissionService;
        this.currentUserService = currentUserService;
    }

    /**
     * Returns the authenticated branch's active admission applications.
     */
    @GetMapping("/applications")
    public ResponseEntity<ApiResponse<Page<ApplicationSummaryDTO>>>
    getBranchApplications(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        CurrentUserContext context =
                currentUserService.getCurrentUserContext(
                        authentication
                );

        Page<ApplicationSummaryDTO> applications =
                admissionService.getBranchApplications(
                        context,
                        page,
                        size
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Applications fetched successfully",
                        applications
                )
        );
    }

    /**
     * Returns the complete application review details only when the
     * application belongs to the authenticated user's branch.
     */
    @GetMapping("/applications/{applicationId}")
    public ResponseEntity<
            ApiResponse<BranchApplicationDetailsResponseDTO>>
    getBranchApplicationDetails(
            Authentication authentication,
            @PathVariable Long applicationId
    ) {
        CurrentUserContext context =
                currentUserService.getCurrentUserContext(
                        authentication
                );

        BranchApplicationDetailsResponseDTO details =
                admissionService.getBranchApplicationDetails(
                        context,
                        applicationId
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Application details fetched successfully",
                        details
                )
        );
    }
}
