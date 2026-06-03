package com.montfort.erp.controller;

import com.montfort.erp.service.ApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/dashboard")
public class DashboardController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ApplicationService applicationService;

    @GetMapping("/stats")
    public ResponseEntity<?> getStats() {
        try {
            Long branchId = applicationService.getCurrentUserBranchId();
            
            if (branchId == null) {
                throw new RuntimeException("Branch ID not found for user.");
            }
            
            // Get total enrolled students
            Integer totalStudents = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM erp_students WHERE branch_id = ?",
                Integer.class,
                branchId
            );
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", Map.of("total_students", totalStudents)
            ));
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(Map.of("success", false, "message", e.getMessage()));
        }
    }
}
