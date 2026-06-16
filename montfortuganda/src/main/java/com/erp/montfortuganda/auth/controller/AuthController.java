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
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import java.util.Map;


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
            // 1. Verify credentials
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            // 2. Load User
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User dbUser = userRepository.findByUsername(userDetails.getUsername()).get();

            // 3. Generate JWT
            String token = jwtUtil.generateToken(userDetails);

            // 4. CREATE THE HTTP-ONLY SECURE COOKIE
            ResponseCookie jwtCookie = ResponseCookie.from("jwt_token", token)
                    .httpOnly(true)
                    .secure(false) // Set to true in Production with HTTPS!
                    .path("/")
                    .maxAge(jwtUtil.getJwtExpirationInMs() / 1000)
                    .sameSite("Strict") // Blocks CSRF attacks
                    .build();

            Integer branchId = dbUser.getAssignedBranch() != null ? dbUser.getAssignedBranch().getBranchId() : null;

            // 5. Return the Response (We NO LONGER send the token in the JSON body!)
            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                    .body(new AuthResponse(null, dbUser.getRole(), branchId));

        } catch (Exception e) {
        // 1. Print the exact error to your Spring Boot console!
        e.printStackTrace();

        // 2. Return a proper JSON object so the frontend doesn't crash!
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Invalid username or password"));
    }
    }

    // --- NEW ENDPOINT: Securely destroy the cookie on logout! ---
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        ResponseCookie cookie = ResponseCookie.from("jwt_token", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0) // 0 instantly destroys the cookie
                .sameSite("Strict")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body("Logged out successfully");
    }
}