package com.erp.montfortuganda.superadmin.controller;

import com.erp.montfortuganda.scholarship.dto.*;
import com.erp.montfortuganda.scholarship.service.ScholarshipService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/superadmin/scholarships")
@PreAuthorize("hasRole('SUPER_ADMIN')")
@RequiredArgsConstructor
public class SuperAdminScholarshipController {

    private final ScholarshipService scholarshipService;

    @GetMapping("/funds-summary")
    public ResponseEntity<Map<String, Object>> getFundsSummary() {
        // Wrapped in "data" to match the Javascript .data extraction
        return ResponseEntity.ok(Map.of("data", scholarshipService.getFundsSummary()));
    }

    @GetMapping("/donors")
    public ResponseEntity<Map<String, Object>> getAllDonors() {
        return ResponseEntity.ok(Map.of("data", scholarshipService.getAllDonors()));
    }

    @GetMapping("/pending-students")
    public ResponseEntity<Map<String, Object>> getPendingStudents() {
        return ResponseEntity.ok(Map.of("data", scholarshipService.getPendingStudents()));
    }

    @GetMapping("/branch-demands")
    public ResponseEntity<Map<String, Object>> getBranchDemands() {
        return ResponseEntity.ok(Map.of("data", scholarshipService.getBranchDemands()));
    }

    @GetMapping("/active-sponsorships")
    public ResponseEntity<Map<String, Object>> getActiveSponsorships() {
        return ResponseEntity.ok(Map.of("data", scholarshipService.getActiveSponsorships()));
    }

    @PostMapping("/allocate-branch")
    public ResponseEntity<String> allocateToBranch(@RequestBody AllocationRequestDTO request) {
        scholarshipService.allocateToBranch(request);
        return ResponseEntity.ok("Successfully allocated funds to Branch");
    }

    @PostMapping("/allocate-student")
    public ResponseEntity<String> allocateToStudent(@RequestBody AllocationRequestDTO request) {
        scholarshipService.allocateToStudent(request);
        return ResponseEntity.ok("Successfully matched Donor to Student");
    }
}