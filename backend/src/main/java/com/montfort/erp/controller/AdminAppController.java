package com.montfort.erp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/applications")
public class AdminAppController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // Helper to get branch ID from the logged-in user
    private Long getCurrentUserBranchId() {
        // Fallback to branch 1 since erp_users does not have branch_id column
        // Alternatively, we could extract branch_id from SecurityContext if it's added there later.
        return 1L;
    }

    @GetMapping("/branch-info")
    public ResponseEntity<?> getBranchInfo() {
        Long branchId = getCurrentUserBranchId();
        String sql = "SELECT branch_id, branch_name, branch_type FROM erp_branches WHERE branch_id = ?";
        List<Map<String, Object>> result = jdbcTemplate.queryForList(sql, branchId);
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
            @RequestParam(required = false) String scholarship) {
            
        Long branchId = getCurrentUserBranchId();
        if (branchId == null) {
            return ResponseEntity.ok(Map.of("success", true, "message", "Branch ID is NULL! Check DB columns.", "data", List.of()));
        }

        StringBuilder sql = new StringBuilder("SELECT app_id, ref_number, student_name, student_surname, applied_class, status, scholarship_status, created_at FROM erp_applications WHERE 1=1 ");
        java.util.List<Object> args = new java.util.ArrayList<>();
        
        // Add branch filtering if needed (currently mocked to return 1)
        // sql.append(" AND branch_id = ? ");
        // args.add(branchId);

        if (status != null && !"All".equalsIgnoreCase(status)) {
            sql.append(" AND status = ? ");
            args.add(status);
        }
        
        if (search != null && !search.trim().isEmpty()) {
            sql.append(" AND (student_name LIKE ? OR student_surname LIKE ? OR ref_number LIKE ?) ");
            String searchParam = "%" + search.trim() + "%";
            args.add(searchParam);
            args.add(searchParam);
            args.add(searchParam);
        }
        
        if (appliedClass != null && !"All".equalsIgnoreCase(appliedClass) && !appliedClass.trim().isEmpty()) {
            sql.append(" AND class_code = ? ");
            args.add(appliedClass);
        } else if (appliedLevel != null && !appliedLevel.trim().isEmpty()) {
            if ("Nursery".equalsIgnoreCase(appliedLevel)) {
                sql.append(" AND class_code IN ('N1', 'N2', 'N3') ");
            } else if ("Primary".equalsIgnoreCase(appliedLevel)) {
                sql.append(" AND class_code IN ('P1', 'P2', 'P3', 'P4', 'P5', 'P6', 'P7') ");
            } else if ("Secondary".equalsIgnoreCase(appliedLevel)) {
                sql.append(" AND class_code IN ('S1', 'S2', 'S3', 'S4', 'S5', 'S6') ");
            }
        }
        
        if (scholarship != null && !"All".equalsIgnoreCase(scholarship) && !scholarship.trim().isEmpty()) {
            sql.append(" AND scholarship_status = ? ");
            args.add(scholarship);
        }
        
        sql.append(" ORDER BY created_at DESC");

        List<Map<String, Object>> applications = jdbcTemplate.queryForList(sql.toString(), args.toArray());

        return ResponseEntity.ok(Map.of("success", true, "data", applications));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> fetchSingle(@PathVariable("id") Long id) {
        Long branchId = getCurrentUserBranchId();
        if (branchId == null) return ResponseEntity.status(401).body(Map.of("success", false, "message", "Unauthorized"));

        String sql = "SELECT * FROM erp_applications WHERE app_id = ? AND branch_id = ?";
        List<Map<String, Object>> result = jdbcTemplate.queryForList(sql, id, branchId);

        if (result.isEmpty()) {
            return ResponseEntity.ok(Map.of("success", false, "message", "Application not found."));
        } else {
            return ResponseEntity.ok(Map.of("success", true, "data", result.get(0)));
        }
    }

    @PostMapping("/status")
    public ResponseEntity<?> updateStatus(@RequestParam("app_id") Long appId, @RequestParam("status") String status) {
        Long branchId = getCurrentUserBranchId();
        if (branchId == null) return ResponseEntity.status(401).body(Map.of("success", false, "message", "Unauthorized"));

        String sql = "UPDATE erp_applications SET status = ? WHERE app_id = ? AND branch_id = ?";
        int updated = jdbcTemplate.update(sql, status, appId, branchId);
        if (updated > 0) {
            return ResponseEntity.ok(Map.of("success", true, "message", "Updated to " + status));
        }
        return ResponseEntity.ok(Map.of("success", false, "message", "Update failed."));
    }

    @PostMapping("/scholarship")
    public ResponseEntity<?> updateScholarship(@RequestParam("app_id") Long appId, @RequestParam("scholarship") String scholarship) {
        Long branchId = getCurrentUserBranchId();
        if (branchId == null) return ResponseEntity.status(401).body(Map.of("success", false, "message", "Unauthorized"));

        String sql = "UPDATE erp_applications SET scholarship_status = ? WHERE app_id = ? AND branch_id = ?";
        int updated = jdbcTemplate.update(sql, scholarship, appId, branchId);
        if (updated > 0) {
            return ResponseEntity.ok(Map.of("success", true, "message", "Scholarship updated"));
        }
        return ResponseEntity.ok(Map.of("success", false, "message", "Update failed."));
    }
}
