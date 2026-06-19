package com.erp.montfortuganda.superadmin;

import com.erp.montfortuganda.dto.ApiResponse;
import com.erp.montfortuganda.scholarship.ScholarshipAllocation;
import com.erp.montfortuganda.scholarship.ScholarshipBranchAllocation;
import com.erp.montfortuganda.scholarship.dto.ScholarshipAllocationRequestDTO;
import com.erp.montfortuganda.scholarship.dto.ScholarshipBranchDemandDTO;
import com.erp.montfortuganda.scholarship.dto.ScholarshipFundsSummaryDTO;
import com.erp.montfortuganda.scholarship.service.ScholarshipService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/superadmin/scholarships")
@PreAuthorize("hasRole('SUPER_ADMIN')")
@RequiredArgsConstructor
public class SuperAdminScholarshipController {

    private final ScholarshipService scholarshipService;

    @GetMapping("/funds-summary")
    public ResponseEntity<ApiResponse<ScholarshipFundsSummaryDTO>> getFundsSummary() {
        return ResponseEntity.ok(ApiResponse.success("Funds summary fetched successfully", scholarshipService.getFundsSummary()));
    }

    @GetMapping("/demands")
    public ResponseEntity<ApiResponse<List<ScholarshipBranchDemandDTO>>> getDemands() {
        return ResponseEntity.ok(ApiResponse.success("Branch demands fetched successfully", scholarshipService.getGlobalDemands()));
    }

    @PostMapping("/allocate/branch")
    public ResponseEntity<ApiResponse<ScholarshipBranchAllocation>> allocateToBranch(@Valid @RequestBody ScholarshipAllocationRequestDTO request) {
        return ResponseEntity.ok(ApiResponse.success("Funds successfully allocated to branch", scholarshipService.allocateToBranch(request)));
    }

    @GetMapping("/history/{branchId}")
    public ResponseEntity<ApiResponse<List<ScholarshipBranchAllocation>>> getBranchHistory(@PathVariable Integer branchId) {
        return ResponseEntity.ok(ApiResponse.success("Branch history fetched successfully", scholarshipService.getBranchHistory(branchId)));
    }

    @PostMapping("/allocate/student")
    public ResponseEntity<ApiResponse<ScholarshipAllocation>> allocateToStudent(@Valid @RequestBody ScholarshipAllocationRequestDTO request) {
        return ResponseEntity.ok(ApiResponse.success("Funds successfully allocated to student", scholarshipService.allocateToStudent(request)));
    }

    @GetMapping("/donors")
    public ResponseEntity<ApiResponse<List<com.erp.montfortuganda.scholarship.dto.ScholarshipDonorDTO>>> getDonors() {
        return ResponseEntity.ok(ApiResponse.success("Donors fetched successfully", scholarshipService.getDonors()));
    }

    @GetMapping("/pending-students")
    public ResponseEntity<ApiResponse<List<com.erp.montfortuganda.scholarship.dto.ScholarshipPendingStudentDTO>>> getPendingStudents() {
        return ResponseEntity.ok(ApiResponse.success("Pending students fetched successfully", scholarshipService.getPendingStudents()));
    }

    @GetMapping("/active-sponsorships")
    public ResponseEntity<ApiResponse<List<com.erp.montfortuganda.scholarship.dto.ScholarshipActiveSponsorshipDTO>>> getActiveSponsorships() {
        return ResponseEntity.ok(ApiResponse.success("Active sponsorships fetched successfully", scholarshipService.getActiveSponsorships()));
    }
}

