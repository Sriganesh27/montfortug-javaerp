package com.erp.montfortuganda.superadmin.controller;

import com.erp.montfortuganda.scholarship.dto.AllocationRequestDTO;
import com.erp.montfortuganda.scholarship.dto.ScholarshipFinalDecisionRequestDTO;
import com.erp.montfortuganda.scholarship.dto.ScholarshipShortlistRequestDTO;
import com.erp.montfortuganda.scholarship.service.ScholarshipService;
import com.erp.montfortuganda.scholarship.service.ScholarshipVerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/superadmin/scholarships")
@PreAuthorize("hasRole('SUPER_ADMIN')")
@RequiredArgsConstructor
public class SuperAdminScholarshipController {

    private final ScholarshipService scholarshipService;
    private final ScholarshipVerificationService scholarshipVerificationService;

    @GetMapping("/funds-summary")
    public ResponseEntity<Map<String, Object>> getFundsSummary() {
        // Wrapped in "data" to match the existing Javascript .data extraction.
        return ResponseEntity.ok(
                Map.of("data", scholarshipService.getFundsSummary())
        );
    }

    @GetMapping("/donors")
    public ResponseEntity<Map<String, Object>> getAllDonors() {
        return ResponseEntity.ok(
                Map.of("data", scholarshipService.getAllDonors())
        );
    }

    @GetMapping("/pending-students")
    public ResponseEntity<Map<String, Object>> getPendingStudents() {
        return ResponseEntity.ok(
                Map.of("data", scholarshipService.getPendingStudents())
        );
    }

    @GetMapping("/branch-demands")
    public ResponseEntity<Map<String, Object>> getBranchDemands() {
        return ResponseEntity.ok(
                Map.of("data", scholarshipService.getBranchDemands())
        );
    }

    @GetMapping("/active-sponsorships")
    public ResponseEntity<Map<String, Object>> getActiveSponsorships() {
        return ResponseEntity.ok(
                Map.of("data", scholarshipService.getActiveSponsorships())
        );
    }

    @PostMapping("/allocate-branch")
    public ResponseEntity<String> allocateToBranch(
            @RequestBody AllocationRequestDTO request
    ) {
        scholarshipService.allocateToBranch(request);
        return ResponseEntity.ok("Successfully allocated funds to Branch");
    }

    @PostMapping("/allocate-student")
    public ResponseEntity<String> allocateToStudent(
            @RequestBody AllocationRequestDTO request
    ) {
        scholarshipService.allocateToStudent(request);
        return ResponseEntity.ok("Successfully matched Donor to Student");
    }

    /*
     * New Scholarship verification/review workflow.
     *
     * These endpoints use the dedicated verification service while the
     * existing ScholarshipService continues to own funds, donors,
     * sponsorships and allocation functionality.
     */

    @GetMapping("/review")
    public ResponseEntity<Map<String, Object>> getScholarshipReviewList() {
        return ResponseEntity.ok(
                Map.of(
                        "data",
                        scholarshipVerificationService.getBranchScholarships()
                )
        );
    }

    @PostMapping("/review/{scholarshipAppId}/shortlist")
    public ResponseEntity<Map<String, Object>> shortlist(
            @PathVariable Long scholarshipAppId,
            @RequestBody ScholarshipShortlistRequestDTO request
    ) {
        scholarshipVerificationService.shortlist(
                scholarshipAppId,
                request
        );

        return ResponseEntity.ok(
                Map.of(
                        "success", true,
                        "message", "Scholarship shortlist decision saved successfully"
                )
        );
    }

    @PostMapping("/review/{scholarshipAppId}/decision")
    public ResponseEntity<Map<String, Object>> finalDecision(
            @PathVariable Long scholarshipAppId,
            @RequestBody ScholarshipFinalDecisionRequestDTO request
    ) {
        scholarshipVerificationService.finalDecision(
                scholarshipAppId,
                request
        );

        return ResponseEntity.ok(
                Map.of(
                        "success", true,
                        "message", "Scholarship final decision saved successfully"
                )
        );
    }
}
