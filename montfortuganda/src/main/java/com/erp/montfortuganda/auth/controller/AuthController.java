package com.erp.montfortuganda.auth.controller;

import com.erp.montfortuganda.auth.User;
import com.erp.montfortuganda.auth.UserRepository;
import com.erp.montfortuganda.auth.dto.AuthRequest;
import com.erp.montfortuganda.auth.dto.AuthResponse;
import com.erp.montfortuganda.auth.jwt.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        try {
            // 1. Verify the username and password with the Database
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            // 2. If correct, load the user details
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User dbUser = userRepository.findByUsername(userDetails.getUsername()).get();

            // 3. Generate the JWT Token (This uses the code that had the yellow warning!)
            String token = jwtUtil.generateToken(userDetails);

            // 4. Return the Token, Role, and Branch ID to the Frontend
            Integer branchId = dbUser.getAssignedBranch() != null ? dbUser.getAssignedBranch().getBranchId() : null;
            return ResponseEntity.ok(new AuthResponse(token, dbUser.getRole(), branchId));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
        }
    }
}