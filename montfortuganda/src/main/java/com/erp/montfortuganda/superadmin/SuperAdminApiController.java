package com.erp.montfortuganda.superadmin;

import com.erp.montfortuganda.dto.ApiResponse;
import com.erp.montfortuganda.school.dto.BranchDTO;
import com.erp.montfortuganda.school.service.BranchService;
import com.erp.montfortuganda.auth.dto.UserDTO;
import com.erp.montfortuganda.auth.service.UserService;
import com.erp.montfortuganda.settings.dto.SiteSettingDTO;
import com.erp.montfortuganda.settings.service.SiteSettingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/api/superadmin")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class SuperAdminApiController {

    @Autowired
    private BranchService branchService;

    @Autowired
    private UserService userService;

    @Autowired
    private SiteSettingService siteSettingService;

    // ==========================================
    // SECURED BRANCH LOGIC
    // ==========================================

    @GetMapping("/branches")
    public ResponseEntity<ApiResponse<List<BranchDTO>>> getAllBranches() {
        return ResponseEntity.ok(ApiResponse.success("Branches fetched successfully", branchService.getAllBranches()));
    }

    @PostMapping(value = "/branches", consumes = {"multipart/form-data"})
    public ResponseEntity<ApiResponse<BranchDTO>> createBranch(
            @ModelAttribute BranchDTO branchDTO,
            @RequestParam(value = "photo", required = false) MultipartFile photo,
            @RequestParam(value = "documents", required = false) List<MultipartFile> documents) {
        return ResponseEntity.ok(ApiResponse.success("Branch created successfully", branchService.createBranch(branchDTO, photo, documents)));
    }

    @PutMapping(value = "/branches/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<ApiResponse<BranchDTO>> updateBranch(
            @PathVariable Integer id,
            @ModelAttribute BranchDTO branchDTO,
            @RequestParam(value = "photo", required = false) MultipartFile photo,
            @RequestParam(value = "documents", required = false) List<MultipartFile> documents) {
        return ResponseEntity.ok(ApiResponse.success("Branch updated successfully", branchService.updateBranch(id, branchDTO, photo, documents)));
    }

    // Toggle active status
    @PutMapping("/branches/{id}/toggle")
    public ResponseEntity<ApiResponse<String>> toggleBranchActive(@PathVariable Integer id) {
        branchService.toggleBranchActive(id);
        return ResponseEntity.ok(ApiResponse.success("Branch status toggled successfully", null));
    }


    // ==========================================
    // SECURED USER LOGIC
    // ==========================================

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserDTO>>> getAllUsers() {
        return ResponseEntity.ok(ApiResponse.success("Users fetched successfully", userService.getAllUsers()));
    }

    @PostMapping("/users")
    public ResponseEntity<ApiResponse<UserDTO>> createUser(@Valid @RequestBody UserDTO userDTO) {
        return ResponseEntity.ok(ApiResponse.success("User created successfully", userService.createUser(userDTO)));
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<ApiResponse<UserDTO>> updateUser(@PathVariable Integer id, @Valid @RequestBody UserDTO userDTO) {
        return ResponseEntity.ok(ApiResponse.success("User updated successfully", userService.updateUser(id, userDTO)));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse<String>> softDeleteUser(@PathVariable Integer id) {
        userService.softDeleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("User successfully deactivated", null));
    }

    // ==========================================
    // SECURED SITE SETTINGS LOGIC
    // ==========================================

    @GetMapping("/settings")
    public ResponseEntity<ApiResponse<List<SiteSettingDTO>>> getAllSettings() {
        return ResponseEntity.ok(ApiResponse.success("Settings fetched successfully", siteSettingService.getAllSettings()));
    }

    @PostMapping("/settings")
    public ResponseEntity<ApiResponse<SiteSettingDTO>> saveSetting(@Valid @RequestBody SiteSettingDTO settingDTO) {
        return ResponseEntity.ok(ApiResponse.success("Setting saved successfully", siteSettingService.saveSetting(settingDTO)));
    }
}