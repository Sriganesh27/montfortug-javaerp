package com.montfort.erp.controller;

import com.montfort.erp.entity.Branch;
import com.montfort.erp.repository.BranchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private BranchRepository branchRepository;

    @GetMapping("/branches")
    public ResponseEntity<List<Branch>> getBranches() {
        return ResponseEntity.ok(branchRepository.findAll());
    }

    @PostMapping("/branches")
    public ResponseEntity<Branch> createBranch(@RequestBody Branch branch) {
        return ResponseEntity.ok(branchRepository.save(branch));
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getStats(org.springframework.security.core.Authentication auth) {
        String username = auth != null ? auth.getName() : "NULL_AUTH";
        return ResponseEntity.ok(java.util.Map.of(
            "totalStudents", 0, // Placeholder
            "userRole", "Admin (" + username + ")"
        ));
    }
}
