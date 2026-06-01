package com.montfort.erp.controller;

import com.montfort.erp.security.AuthRequest;
import com.montfort.erp.security.AuthResponse;
import com.montfort.erp.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        try {
            UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
            
            // Check password (assuming bcrypt)
            if (passwordEncoder.matches(request.getPassword(), userDetails.getPassword())) {
                String token = jwtUtil.generateToken(userDetails);
                return ResponseEntity.ok(new AuthResponse(token, userDetails.getUsername(), request.getRole()));
            } else {
                return ResponseEntity.status(401).body(java.util.Map.of("message", "Invalid credentials"));
            }
        } catch (org.springframework.security.core.userdetails.UsernameNotFoundException e) {
            return ResponseEntity.status(401).body(java.util.Map.of("message", "User not found"));
        } catch (Exception e) {
            e.printStackTrace(); // Log the error to console for debugging
            return ResponseEntity.status(401).body(java.util.Map.of("message", "System Error: " + e.getMessage()));
        }
    }
}
