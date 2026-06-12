package com.montfort.erp.modules.applications.controller;

import com.montfort.erp.modules.applications.service.ApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/applications")
public class AdminAppController {

    @Autowired
    private ApplicationService applicationService;

    @GetMapping("/branch-info")
    public ResponseEntity<?> getBranchInfo() {
        List<Map<String, Object>> result = applicationService.getBranchInfo();
        if (result.isEmpty()) {
            return ResponseEntity.ok(Map.of("success", false));
        }
        return ResponseEntity.ok(Map.of("success", true, "data", result.get(0)));
    }

    @GetMapping
    public ResponseEntity<?> fetchList(
            @RequestParam(defaultValue = "All") String status,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String appliedLevel,
            @RequestParam(required = false) String appliedClass,
            @RequestParam(required = false) String scholarship,
            @RequestParam(required = false) String academicYear) {
            
        Long branchId = applicationService.getCurrentUserBranchId();
        if (branchId == null) {
            return ResponseEntity.ok(Map.of("success", true, "message", "Branch ID is NULL! Check DB columns.", "data", List.of()));
        }

        List<Map<String, Object>> applications = applicationService.fetchApplications(status, search, appliedLevel, appliedClass, scholarship, academicYear);
        return ResponseEntity.ok(Map.of("success", true, "data", applications));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> fetchSingle(@PathVariable("id") Long id) {
        List<Map<String, Object>> result = applicationService.fetchSingleApplication(id);

        if (result.isEmpty()) {
            return ResponseEntity.ok(Map.of("success", false, "message", "Application not found."));
        } else {
            return ResponseEntity.ok(Map.of("success", true, "data", result.get(0)));
        }
    }

    @PostMapping("/status")
    public ResponseEntity<?> updateStatus(@RequestParam("app_id") Long appId, @RequestParam("status") String status) {
        boolean updated = applicationService.updateStatus(appId, status);
        if (updated) {
            return ResponseEntity.ok(Map.of("success", true, "message", "Updated to " + status));
        }
        return ResponseEntity.ok(Map.of("success", false, "message", "Update failed."));
    }

    @PostMapping("/scholarship")
    public ResponseEntity<?> updateScholarship(@RequestParam("app_id") Long appId, @RequestParam("scholarship") String scholarship) {
        boolean updated = applicationService.updateScholarship(appId, scholarship);
        if (updated) {
            return ResponseEntity.ok(Map.of("success", true, "message", "Scholarship updated"));
        }
        return ResponseEntity.ok(Map.of("success", false, "message", "Update failed."));
    }
}

