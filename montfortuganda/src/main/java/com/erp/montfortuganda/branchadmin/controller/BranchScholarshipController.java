package com.erp.montfortuganda.branchadmin.controller;

import com.erp.montfortuganda.dto.ApiResponse;
import com.erp.montfortuganda.scholarship.dto.ScholarshipApplicationFormResponseDTO;
import com.erp.montfortuganda.scholarship.dto.ScholarshipApplicationListItemDTO;
import com.erp.montfortuganda.scholarship.dto.ScholarshipApplicationSearchRequestDTO;
import com.erp.montfortuganda.scholarship.dto.ScholarshipBranchDistributionCompletionRequestDTO;
import com.erp.montfortuganda.scholarship.dto.ScholarshipShortlistRequestDTO;
import com.erp.montfortuganda.scholarship.dto.ScholarshipFinalDecisionRequestDTO;
import com.erp.montfortuganda.scholarship.dto.ScholarshipVerificationAssignmentRequestDTO;
import com.erp.montfortuganda.scholarship.dto.ScholarshipVerificationCompletionRequestDTO;
import com.erp.montfortuganda.scholarship.service.ScholarshipVerificationService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Branch Admin Scholarship management endpoints.
 *
 * <p>Branch identity is resolved by the Scholarship service from the
 * authenticated Branch Admin context. The browser does not supply a
 * branch ID.</p>
 *
 * <p>The final Super Admin decision endpoint is retained for compatibility
 * with the existing route contract, but is explicitly restricted to the
 * SUPER_ADMIN role. The dedicated Super Admin controller remains the normal
 * entry point for that operation.</p>
 */
@RestController
@RequestMapping("/api/branchadmin/scholarships")
@PreAuthorize("hasRole('BRANCH_ADMIN')")
public class BranchScholarshipController {

    private final ScholarshipVerificationService scholarshipVerificationService;

    public BranchScholarshipController(
            ScholarshipVerificationService scholarshipVerificationService
    ) {
        this.scholarshipVerificationService =
                scholarshipVerificationService;
    }

    @GetMapping("/funds-summary")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getFundSummary() {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Branch Scholarship fund summary fetched successfully",
                        scholarshipVerificationService.getBranchFundSummary()
                )
        );
    }

    @GetMapping("/overview")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getScholarshipOverview(
            @RequestParam(required = false) String academicYear,
            @RequestParam(required = false) String term,
            @RequestParam(required = false) Integer levelId,
            @RequestParam(required = false) Integer classId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Branch Scholarship overview fetched successfully",
                        scholarshipVerificationService.getBranchScholarshipOverview(
                                academicYear,
                                term,
                                levelId,
                                classId
                        )
                )
        );
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<ScholarshipApplicationListItemDTO>>>
    searchScholarships(
            @ModelAttribute ScholarshipApplicationSearchRequestDTO request
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Scholarship applications searched successfully",
                        scholarshipVerificationService.searchBranchScholarships(
                                request
                        )
                )
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>>
    getScholarships() {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Scholarship applications fetched successfully",
                        scholarshipVerificationService.getBranchScholarships()
                )
        );
    }

    @GetMapping("/{scholarshipAppId}")
    public ResponseEntity<ApiResponse<ScholarshipApplicationFormResponseDTO>>
    getScholarshipDetail(
            @PathVariable Long scholarshipAppId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Scholarship application fetched successfully",
                        scholarshipVerificationService.getBranchScholarshipDetail(
                                scholarshipAppId
                        )
                )
        );
    }

    @GetMapping("/verification-employees")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>>
    getVerificationEmployees() {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Verification employees fetched successfully",
                        scholarshipVerificationService.getVerificationEmployees()
                )
        );
    }

    @PostMapping("/{scholarshipAppId}/verification/assign")
    public ResponseEntity<ApiResponse<Void>> assignVerificationEmployee(
            @PathVariable Long scholarshipAppId,
            @RequestBody ScholarshipVerificationAssignmentRequestDTO request
    ) {
        scholarshipVerificationService.assignVerificationEmployee(
                scholarshipAppId,
                request
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Scholarship verification employee assigned successfully",
                        null
                )
        );
    }

    @PostMapping("/{scholarshipAppId}/verification/complete")
    public ResponseEntity<ApiResponse<Void>> completeVerification(
            @PathVariable Long scholarshipAppId,
            @RequestBody ScholarshipVerificationCompletionRequestDTO request
    ) {
        scholarshipVerificationService.completeVerification(
                scholarshipAppId,
                request
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Scholarship verification completed successfully",
                        null
                )
        );
    }

    @PostMapping("/{scholarshipAppId}/shortlist")
    public ResponseEntity<ApiResponse<Void>> shortlist(
            @PathVariable Long scholarshipAppId,
            @RequestBody ScholarshipShortlistRequestDTO request
    ) {
        scholarshipVerificationService.shortlist(
                scholarshipAppId,
                request
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Scholarship shortlist decision saved successfully",
                        null
                )
        );
    }

    /**
     * Makes the later Branch Admin decision for a Scholarship that is
     * currently WAITLISTED.
     *
     * <p>Allowed decisions are SHORTLISTED or REJECTED. The service remains
     * responsible for validating the current workflow stage, branch
     * ownership, rejection remarks and notification behavior.</p>
     */
    @PostMapping("/{scholarshipAppId}/waitlist/decision")
    public ResponseEntity<ApiResponse<Void>> decideFromWaitlist(
            @PathVariable Long scholarshipAppId,
            @RequestBody ScholarshipShortlistRequestDTO request
    ) {
        scholarshipVerificationService.decideFromWaitlist(
                scholarshipAppId,
                request
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Scholarship waitlist decision saved successfully",
                        null
                )
        );
    }

    /**
     * Completes the Branch Admin distribution step for the exact
     * Scholarship History/cycle.
     */
    @PostMapping("/{scholarshipAppId}/distribution/complete")
    public ResponseEntity<ApiResponse<Void>> completeBranchDistribution(
            @PathVariable Long scholarshipAppId,
            @RequestBody ScholarshipBranchDistributionCompletionRequestDTO request
    ) {
        scholarshipVerificationService.completeBranchDistribution(
                scholarshipAppId,
                request
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Scholarship distribution completed successfully",
                        null
                )
        );
    }

    /**
     * Compatibility route for the existing API contract.
     *
     * <p>Final Scholarship authority belongs to Super Admin, therefore this
     * endpoint is additionally restricted to SUPER_ADMIN.</p>
     */
    @PostMapping("/{scholarshipAppId}/final-decision")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> finalDecision(
            @PathVariable Long scholarshipAppId,
            @RequestBody ScholarshipFinalDecisionRequestDTO request
    ) {
        scholarshipVerificationService.finalDecision(
                scholarshipAppId,
                request
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Scholarship final decision saved successfully",
                        null
                )
        );
    }
}
