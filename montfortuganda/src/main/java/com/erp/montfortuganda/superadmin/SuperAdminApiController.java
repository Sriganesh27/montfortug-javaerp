package com.erp.montfortuganda.superadmin;

import com.erp.montfortuganda.auth.User;
import com.erp.montfortuganda.auth.UserRepository;
import com.erp.montfortuganda.school.Branch;
import com.erp.montfortuganda.school.BranchRepository;
import com.erp.montfortuganda.settings.SiteSetting;
import com.erp.montfortuganda.settings.SiteSettingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/api/superadmin")
@PreAuthorize("hasRole('Super User')")
public class SuperAdminApiController {

    @Autowired
    private BranchRepository branchRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SiteSettingRepository siteSettingRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ===========================
    // BRANCH (CAMPUS) MANAGEMENT
    // ===========================

    @GetMapping("/branches")
    public List<Branch> getAllBranches() {
        return branchRepository.findAll();
    }

    @PostMapping("/branches")
    public Branch createBranch(@RequestBody Branch branch) {
        branch.setIsActive(1); // Default to active on creation
        return branchRepository.save(branch);
    }

    @PutMapping("/branches/{id}")
    public ResponseEntity<Branch> updateBranch(@PathVariable Integer id, @RequestBody Branch updatedBranch) {
        return branchRepository.findById(id).map(branch -> {
            branch.setBranchName(updatedBranch.getBranchName());
            branch.setSchoolCode(updatedBranch.getSchoolCode());
            branch.setBranchType(updatedBranch.getBranchType());
            branch.setBranchLocation(updatedBranch.getBranchLocation());
            return ResponseEntity.ok(branchRepository.save(branch));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/branches/{id}")
    public ResponseEntity<?> softDeleteBranch(@PathVariable Integer id) {
        return branchRepository.findById(id).map(branch -> {
            branch.setIsActive(0); // Soft delete
            branchRepository.save(branch);
            return ResponseEntity.ok("Campus successfully deactivated.");
        }).orElse(ResponseEntity.notFound().build());
    }

    // ===========================
    // USER MANAGEMENT
    // ===========================

    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody User user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Error: Username is already taken!");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setIsActive(1);
        return ResponseEntity.ok(userRepository.save(user));
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Integer id, @RequestBody User updatedUser) {
        return userRepository.findById(id).map(user -> {
            user.setRole(updatedUser.getRole());
            user.setAssignedBranch(updatedUser.getAssignedBranch());
            if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
                user.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
            }
            return ResponseEntity.ok(userRepository.save(user));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> softDeleteUser(@PathVariable Integer id) {
        return userRepository.findById(id).map(user -> {
            user.setIsActive(0); // Soft delete
            userRepository.save(user);
            return ResponseEntity.ok("User successfully deactivated.");
        }).orElse(ResponseEntity.notFound().build());
    }

    // ===========================
    // SITE SETTINGS MANAGEMENT
    // ===========================

    @GetMapping("/settings")
    public List<SiteSetting> getAllSettings() {
        return siteSettingRepository.findAll();
    }

    @PostMapping("/settings")
    public SiteSetting saveSetting(@RequestBody SiteSetting setting) {
        return siteSettingRepository.findBySettingKey(setting.getSettingKey())
                .map(existing -> {
                    existing.setSettingValue(setting.getSettingValue());
                    return siteSettingRepository.save(existing);
                })
                .orElseGet(() -> siteSettingRepository.save(setting));
    }
}