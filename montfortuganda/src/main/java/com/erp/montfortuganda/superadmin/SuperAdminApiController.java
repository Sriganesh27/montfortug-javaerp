package com.erp.montfortuganda.superadmin;

import com.erp.montfortuganda.dto.ApiResponse;
import com.erp.montfortuganda.school.dto.BranchDTO;
import com.erp.montfortuganda.school.service.BranchService;
import com.erp.montfortuganda.auth.User;
import com.erp.montfortuganda.auth.UserRepository;
import com.erp.montfortuganda.settings.SiteSetting;
import com.erp.montfortuganda.settings.SiteSettingRepository;
import jakarta.validation.Valid;
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
    private BranchService branchService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SiteSettingRepository siteSettingRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ==========================================
    // SECURED BRANCH LOGIC (VIA SERVICE & DTO)
    // ==========================================

    @GetMapping("/branches")
    public ResponseEntity<ApiResponse<List<BranchDTO>>> getAllBranches() {
        List<BranchDTO> branches = branchService.getAllBranches();
        return ResponseEntity.ok(ApiResponse.success("Branches fetched successfully", branches));
    }

    @PostMapping("/branches")
    public ResponseEntity<ApiResponse<BranchDTO>> createBranch(@Valid @RequestBody BranchDTO branchDTO) {
        BranchDTO createdBranch = branchService.createBranch(branchDTO);
        return ResponseEntity.ok(ApiResponse.success("Branch created successfully", createdBranch));
    }

    @PutMapping("/branches/{id}")
    public ResponseEntity<ApiResponse<BranchDTO>> updateBranch(@PathVariable Integer id, @Valid @RequestBody BranchDTO branchDTO) {
        BranchDTO updatedBranch = branchService.updateBranch(id, branchDTO);
        return ResponseEntity.ok(ApiResponse.success("Branch updated successfully", updatedBranch));
    }

    @DeleteMapping("/branches/{id}")
    public ResponseEntity<ApiResponse<String>> softDeleteBranch(@PathVariable Integer id) {
        branchService.softDeleteBranch(id);
        return ResponseEntity.ok(ApiResponse.success("Branch successfully deactivated", null));
    }

    // ===========================
    // USER MANAGEMENT
    // ===========================
    @GetMapping("/users")
    public List<User> getAllUsers() { return userRepository.findAll(); }

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
            user.setIsActive(0);
            userRepository.save(user);
            return ResponseEntity.ok("User successfully deactivated.");
        }).orElse(ResponseEntity.notFound().build());
    }

    // ===========================
    // SITE SETTINGS MANAGEMENT
    // ===========================
    @GetMapping("/settings")
    public List<SiteSetting> getAllSettings() { return siteSettingRepository.findAll(); }

    @PostMapping("/settings")
    public SiteSetting saveSetting(@RequestBody SiteSetting setting) {
        return siteSettingRepository.findBySettingKey(setting.getSettingKey())
                .map(existing -> {
                    existing.setSettingValue(setting.getSettingValue());
                    return siteSettingRepository.save(existing);
                }).orElseGet(() -> siteSettingRepository.save(setting));
    }
}