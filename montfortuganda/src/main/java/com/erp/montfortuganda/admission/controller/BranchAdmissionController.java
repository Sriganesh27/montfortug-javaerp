package com.erp.montfortuganda.admission.controller;

import com.erp.montfortuganda.admission.dto.ApplicationSummaryDTO;
import com.erp.montfortuganda.admission.dto.BranchApplicationDetailsResponseDTO;
import com.erp.montfortuganda.admission.service.BranchAdmissionService;
import com.erp.montfortuganda.auth.service.CurrentUserContext;
import com.erp.montfortuganda.auth.service.CurrentUserService;
import com.erp.montfortuganda.dto.ApiResponse;
import com.erp.montfortuganda.scholarship.dto.ScholarshipApplicationFormRequestDTO;
import com.erp.montfortuganda.scholarship.dto.ScholarshipApplicationFormResponseDTO;
import com.erp.montfortuganda.scholarship.service.ScholarshipService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
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
    private final ScholarshipService scholarshipService;

    public BranchAdmissionController(
            BranchAdmissionService admissionService,
            CurrentUserService currentUserService,
            ScholarshipService scholarshipService
    ) {
        this.admissionService = admissionService;
        this.currentUserService = currentUserService;
        this.scholarshipService = scholarshipService;
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

    @GetMapping("/scholarship/application-form")
    public ResponseEntity<ApiResponse<ScholarshipApplicationFormResponseDTO>>
    getSchoolScholarshipApplicationForm(
            @RequestHeader("X-Scholarship-School-Access")
            String accessKey
    ) {
        ScholarshipApplicationFormResponseDTO response =
                scholarshipService.getApplicationFormForSchoolAccess(
                        accessKey
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Scholarship application form fetched successfully",
                        response
                )
        );
    }

    @PatchMapping("/scholarship/application-form")
    public ResponseEntity<ApiResponse<ScholarshipApplicationFormResponseDTO>>
    saveSchoolScholarshipApplicationForm(
            @RequestHeader("X-Scholarship-School-Access")
            String accessKey,
            @RequestBody ScholarshipApplicationFormRequestDTO request
    ) {
        ScholarshipApplicationFormResponseDTO response =
                scholarshipService.saveApplicationFormForSchoolAccess(
                        accessKey,
                        request
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Scholarship application draft saved successfully",
                        response
                )
        );
    }

    @PostMapping("/scholarship/application-form/submit")
    public ResponseEntity<ApiResponse<ScholarshipApplicationFormResponseDTO>>
    submitSchoolScholarshipApplicationForm(
            @RequestHeader("X-Scholarship-School-Access")
            String accessKey,
            @RequestBody ScholarshipApplicationFormRequestDTO request
    ) {
        ScholarshipApplicationFormResponseDTO response =
                scholarshipService.submitApplicationFormForSchoolAccess(
                        accessKey,
                        request
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Scholarship application submitted successfully",
                        response
                )
        );
    }

}
