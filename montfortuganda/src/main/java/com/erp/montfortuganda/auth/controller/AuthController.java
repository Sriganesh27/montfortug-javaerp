package com.erp.montfortuganda.auth.controller;

import com.erp.montfortuganda.auth.User;
import com.erp.montfortuganda.auth.repository.UserRepository;
import com.erp.montfortuganda.auth.dto.AuthRequest;
import com.erp.montfortuganda.auth.dto.AuthResponse;
import com.erp.montfortuganda.auth.jwt.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    // 1. Professional Logger added to replace printStackTrace
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    // 2. Removed unused PasswordEncoder!

    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil, UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        try {
            // 1. Verify credentials
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            // 2. Load User safely
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            // This totally satisfies IntelliJ. It proves userDetails can NEVER be null past this line!
            if (userDetails == null) {
                throw new Exception("Authentication failed: User session is invalid.");
            }
            User dbUser = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new Exception("User not found in database"));
            // --- THE BULLETPROOF SECURITY CHECK ---
            String dbRole = dbUser.getRole() != null ? dbUser.getRole().toUpperCase() : "";
            boolean isAdmin = dbRole.equals("SUPER_ADMIN") || dbRole.equals("ROLE_SUPER_ADMIN") || dbRole.equals("BRANCH_ADMIN");

            if (isAdmin && !"SECURE_ADMIN_GATEWAY".equals(request.getRole())) {
                throw new Exception("Security Alert: Admins cannot log in from the public portal.");
            }
            // ---------------------------------------

            // 3. Generate JWT
            String token = jwtUtil.generateToken(userDetails);

            // 4. CREATE THE HTTP-ONLY SECURE COOKIE
            ResponseCookie jwtCookie = ResponseCookie.from("jwt_token", token)
                    .httpOnly(true)
                    .secure(true)
                    .path("/")
                    .maxAge(jwtUtil.getJwtExpirationInMs() / 1000)
                    .sameSite("Strict")
                    .build();

            Integer branchId = dbUser.getAssignedBranch() != null ? dbUser.getAssignedBranch().getBranchId() : null;

            // 5. Return the Response
            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                    .body(new AuthResponse(null, dbUser.getRole(), branchId));

        } catch (Exception e) {
            // 3. Robust logging instead of printStackTrace
            logger.error("Login attempt failed: {}", e.getMessage());

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Invalid username or password"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        ResponseCookie cookie = ResponseCookie.from("jwt_token", "")
                .httpOnly(true)
                .secure(true) //
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(Map.of("message", "Logged out successfully"));
    }
}