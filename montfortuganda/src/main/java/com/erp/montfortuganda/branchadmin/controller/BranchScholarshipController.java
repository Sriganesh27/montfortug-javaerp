package com.erp.montfortuganda.branchadmin.controller;

import com.erp.montfortuganda.dto.ApiResponse;
import com.erp.montfortuganda.scholarship.dto.ScholarshipApplicationFormResponseDTO;
import com.erp.montfortuganda.scholarship.dto.ScholarshipFinalDecisionRequestDTO;
import com.erp.montfortuganda.scholarship.dto.ScholarshipShortlistRequestDTO;
import com.erp.montfortuganda.scholarship.dto.ScholarshipVerificationAssignmentRequestDTO;
import com.erp.montfortuganda.scholarship.dto.ScholarshipVerificationCompletionRequestDTO;
import com.erp.montfortuganda.scholarship.service.ScholarshipVerificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/branchadmin/scholarships")
@PreAuthorize("hasRole('BRANCH_ADMIN')")
public class BranchScholarshipController {

    private final ScholarshipVerificationService scholarshipVerificationService;

    public BranchScholarshipController(
            ScholarshipVerificationService scholarshipVerificationService
    ) {
        this.scholarshipVerificationService = scholarshipVerificationService;
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

    @GetMapping
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getScholarships() {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Scholarship applications fetched successfully",
                        scholarshipVerificationService.getBranchScholarships()
                )
        );
    }

    @GetMapping("/{scholarshipAppId}")
    public ResponseEntity<ApiResponse<ScholarshipApplicationFormResponseDTO>>
    getScholarshipDetail(@PathVariable Long scholarshipAppId) {
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
                scholarshipAppId, request
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
                scholarshipAppId, request
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
                scholarshipAppId, request
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Scholarship shortlist decision saved successfully",
                        null
                )
        );
    }

    @PostMapping("/{scholarshipAppId}/final-decision")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> finalDecision(
            @PathVariable Long scholarshipAppId,
            @RequestBody ScholarshipFinalDecisionRequestDTO request
    ) {
        scholarshipVerificationService.finalDecision(
                scholarshipAppId, request
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Scholarship final decision saved successfully",
                        null
                )
        );
    }
}
