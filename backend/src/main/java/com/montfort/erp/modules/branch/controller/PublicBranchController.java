package com.montfort.erp.modules.branch.controller;

import com.montfort.erp.modules.applications.service.ApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/public/branches")
public class PublicBranchController {

    @Autowired
    private ApplicationService applicationService;

    @GetMapping
    public ResponseEntity<?> getBranches() {
        try {
            List<Map<String, Object>> branches = applicationService.getAllBranches();
            return ResponseEntity.ok(Map.of("success", true, "data", branches));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(Map.of("success", false, "message", "Server Error: " + e.getMessage()));
        }
    }
}

