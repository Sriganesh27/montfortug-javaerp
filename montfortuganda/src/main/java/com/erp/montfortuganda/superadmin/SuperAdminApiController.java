package com.erp.montfortuganda.superadmin;

import com.erp.montfortuganda.auth.dto.UserDTO;
import com.erp.montfortuganda.auth.service.UserService;
import com.erp.montfortuganda.dto.ApiResponse;
import com.erp.montfortuganda.school.dto.BranchDTO;
import com.erp.montfortuganda.school.service.BranchService;
import com.erp.montfortuganda.settings.dto.SiteSettingDTO;
import com.erp.montfortuganda.settings.service.SiteSettingService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/superadmin")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class SuperAdminApiController {

    private final BranchService branchService;
    private final UserService userService;
    private final SiteSettingService siteSettingService;

    public SuperAdminApiController(
            BranchService branchService,
            UserService userService,
            SiteSettingService siteSettingService
    ) {
        this.branchService = branchService;
        this.userService = userService;
        this.siteSettingService = siteSettingService;
    }

    // ==========================================
    // BRANCH MANAGEMENT
    // ==========================================

    @GetMapping("/branches")
    public ResponseEntity<ApiResponse<List<BranchDTO>>>
    getAllBranches() {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Branches fetched successfully",
                        branchService.getAllBranches()
                )
        );
    }

    @GetMapping("/branches/{id}")
    public ResponseEntity<ApiResponse<BranchDTO>>
    getBranchById(
            @PathVariable Integer id
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Branch fetched successfully",
                        branchService.getBranchById(id)
                )
        );
    }

    @PostMapping(
            value = "/branches",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ApiResponse<BranchDTO>>
    createBranch(
            @Valid @ModelAttribute BranchDTO branchDTO,
            @RequestParam(
                    value = "logo",
                    required = false
            )
            MultipartFile logo,
            @RequestParam(
                    value = "photo",
                    required = false
            )
            MultipartFile photo,
            @RequestParam(
                    value = "documents",
                    required = false
            )
            List<MultipartFile> documents
    ) {
        BranchDTO createdBranch =
                branchService.createBranch(
                        branchDTO,
                        logo,
                        photo,
                        documents
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Branch created successfully",
                        createdBranch
                )
        );
    }

    @PutMapping(
            value = "/branches/{id}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ApiResponse<BranchDTO>>
    updateBranch(
            @PathVariable Integer id,
            @Valid @ModelAttribute BranchDTO branchDTO,
            @RequestParam(
                    value = "logo",
                    required = false
            )
            MultipartFile logo,
            @RequestParam(
                    value = "photo",
                    required = false
            )
            MultipartFile photo,
            @RequestParam(
                    value = "documents",
                    required = false
            )
            List<MultipartFile> documents
    ) {
        BranchDTO updatedBranch =
                branchService.updateBranch(
                        id,
                        branchDTO,
                        logo,
                        photo,
                        documents
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Branch updated successfully",
                        updatedBranch
                )
        );
    }

    @PutMapping("/branches/{id}/toggle")
    public ResponseEntity<ApiResponse<String>>
    toggleBranchActive(
            @PathVariable Integer id
    ) {
        branchService.toggleBranchActive(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Branch status toggled successfully",
                        null
                )
        );
    }

    @PutMapping("/branches/{id}/reset-admin-password")
    public ResponseEntity<ApiResponse<String>>
    resetBranchAdminPassword(
            @PathVariable Integer id
    ) {
        branchService.resetBranchAdminPassword(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "New Branch Admin credentials are being sent to the branch email.",
                        null
                )
        );
    }

    // ==========================================
    // TEMPORARY BRANCH STATS AND LOGS
    // ==========================================

    @GetMapping("/branches/{id}/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>>
    getBranchStats(
            @PathVariable Integer id
    ) {
        branchService.getBranchById(id);

        Map<String, Object> stats =
                new HashMap<>();

        stats.put("students", 0);
        stats.put("staff", 0);
        stats.put("attendance", 100);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Stats fetched",
                        stats
                )
        );
    }

    @GetMapping("/branches/{id}/logs")
    public ResponseEntity<ApiResponse<List<Map<String, String>>>>
    getBranchLogs(
            @PathVariable Integer id
    ) {
        branchService.getBranchById(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Logs fetched",
                        List.of()
                )
        );
    }

    // ==========================================
    // USER MANAGEMENT
    // ==========================================

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserDTO>>>
    getAllUsers() {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Users fetched successfully",
                        userService.getAllUsers()
                )
        );
    }

    @PostMapping("/users")
    public ResponseEntity<ApiResponse<UserDTO>>
    createUser(
            @Valid @RequestBody UserDTO userDTO
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "User created successfully",
                        userService.createUser(userDTO)
                )
        );
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<ApiResponse<UserDTO>>
    updateUser(
            @PathVariable Integer id,
            @Valid @RequestBody UserDTO userDTO
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "User updated successfully",
                        userService.updateUser(
                                id,
                                userDTO
                        )
                )
        );
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse<String>>
    softDeleteUser(
            @PathVariable Integer id
    ) {
        userService.softDeleteUser(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "User successfully deactivated",
                        null
                )
        );
    }

    // ==========================================
    // SITE SETTINGS
    // ==========================================

    @GetMapping("/settings")
    public ResponseEntity<ApiResponse<List<SiteSettingDTO>>>
    getAllSettings() {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Settings fetched successfully",
                        siteSettingService.getAllSettings()
                )
        );
    }

    @PostMapping("/settings")
    public ResponseEntity<ApiResponse<SiteSettingDTO>>
    saveSetting(
            @Valid @RequestBody SiteSettingDTO settingDTO
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Setting saved successfully",
                        siteSettingService.saveSetting(
                                settingDTO
                        )
                )
        );
    }
}
