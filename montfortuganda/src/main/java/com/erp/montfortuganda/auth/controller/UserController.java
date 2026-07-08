package com.erp.montfortuganda.auth.controller;

import com.erp.montfortuganda.auth.User;
import com.erp.montfortuganda.auth.repository.UserRepository;
import com.erp.montfortuganda.auth.dto.UserRequest;
import com.erp.montfortuganda.school.Branch;
import com.erp.montfortuganda.school.BranchRepository;
import com.erp.montfortuganda.auth.ErpUserRole;
import com.erp.montfortuganda.auth.repository.ErpRoleRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;
    private final BranchRepository branchRepository;
    private final PasswordEncoder passwordEncoder;
    private final ErpRoleRepository erpRoleRepository; // NEW: Added to fields

    public UserController(UserRepository userRepository, BranchRepository branchRepository, PasswordEncoder passwordEncoder, ErpRoleRepository erpRoleRepository) { // NEW: Added to constructor
        this.userRepository = userRepository;
        this.branchRepository = branchRepository;
        this.passwordEncoder = passwordEncoder;
        this.erpRoleRepository = erpRoleRepository; // NEW: Initialized
    }

    // NEW: Allow Super Admin to create new School Admins
    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> createUser(@RequestBody UserRequest request) {

        // 1. Check if username already exists
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Username is already taken!");
        }

        // 2. Find the school they are being assigned to
        Branch branch = branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new RuntimeException("School not found!"));

        // 3. Create the User and encrypt their password
        User newUser = new User();
        newUser.setUsername(request.getUsername());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));

        // Enforce strict uppercase standard (e.g. "School Admin" becomes "BRANCH_ADMIN")
        String finalRoleCode = request.getRole().equalsIgnoreCase("School Admin") ? "BRANCH_ADMIN" : request.getRole().toUpperCase().replace(" ", "_");
        newUser.setRole(finalRoleCode); // Legacy fallback string
        newUser.setAssignedBranch(branch);
        newUser.setIsActive(1);

        // --- NEW RBAC MAPPING ---
        erpRoleRepository.findByRoleCode(finalRoleCode).ifPresent(role -> {
            ErpUserRole userRole = new ErpUserRole();
            userRole.setRole(role);
            userRole.setActive(true);
            newUser.addRole(userRole);
        });
        // ------------------------

        userRepository.save(newUser);

        return ResponseEntity.ok("User created successfully!");
    }
}