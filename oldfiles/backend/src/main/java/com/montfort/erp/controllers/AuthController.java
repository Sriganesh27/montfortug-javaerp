package com.montfort.erp.controllers;

import com.montfort.erp.dto.AuthResponse;
import com.montfort.erp.dto.LoginRequest;
import com.montfort.erp.security.CustomUserDetails;
import com.montfort.erp.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String jwt = jwtUtil.generateToken(userDetails);

        Integer branchId = null;
        if(userDetails.getUser().getAssignedBranch() != null){
            branchId = userDetails.getUser().getAssignedBranch().getBranchId();
        }

        return ResponseEntity.ok(new AuthResponse(
                jwt,
                userDetails.getUser().getRole(),
                branchId,
                userDetails.getUsername()
        ));
    }
}
