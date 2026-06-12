package com.erp.montfortuganda.auth.controller;

import com.erp.montfortuganda.auth.User;
import com.erp.montfortuganda.auth.UserRepository;
import com.erp.montfortuganda.auth.dto.UserRequest;
import com.erp.montfortuganda.school.Branch;
import com.erp.montfortuganda.school.BranchRepository;
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

    public UserController(UserRepository userRepository, BranchRepository branchRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.branchRepository = branchRepository;
        this.passwordEncoder = passwordEncoder;
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
        newUser.setRole(request.getRole());
        newUser.setAssignedBranch(branch);
        newUser.setIsActive(1);

        userRepository.save(newUser);

        return ResponseEntity.ok("User created successfully!");
    }
}