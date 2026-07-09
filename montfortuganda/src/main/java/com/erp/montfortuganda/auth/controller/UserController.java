package com.erp.montfortuganda.auth.controller;

import com.erp.montfortuganda.auth.User;
import com.erp.montfortuganda.auth.repository.UserRepository;
import com.erp.montfortuganda.auth.dto.UserRequest;
import com.erp.montfortuganda.school.Branch;
import com.erp.montfortuganda.school.repository.BranchRepository;
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
    private final ErpRoleRepository erpRoleRepository;

    public UserController(UserRepository userRepository, BranchRepository branchRepository, PasswordEncoder passwordEncoder, ErpRoleRepository erpRoleRepository) {
        this.userRepository = userRepository;
        this.branchRepository = branchRepository;
        this.passwordEncoder = passwordEncoder;
        this.erpRoleRepository = erpRoleRepository;
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> createUser(@RequestBody UserRequest request) {

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Username is already taken!");
        }

        // FIXED: Using request.getBranchId() with proper generic inference
        Branch branch = branchRepository.findById(request.getBranchId().longValue())
                .orElseThrow(() -> new IllegalArgumentException("Branch not found"));

        User newUser = new User();
        newUser.setUsername(request.getUsername());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));

        String finalRoleCode = request.getRole().equalsIgnoreCase("School Admin") ? "BRANCH_ADMIN" : request.getRole().toUpperCase().replace(" ", "_");
        newUser.setRole(finalRoleCode);
        newUser.setAssignedBranch(branch);
        newUser.setIsActive(1);

        erpRoleRepository.findByRoleCode(finalRoleCode).ifPresent(role -> {
            ErpUserRole userRole = new ErpUserRole();
            userRole.setRole(role);
            userRole.setActive(true);
            newUser.addRole(userRole);
        });

        userRepository.save(newUser);

        return ResponseEntity.ok("User created successfully!");
    }
}