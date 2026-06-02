package com.montfort.erp.service;

import com.montfort.erp.dto.AuthRequest;
import com.montfort.erp.dto.AuthResponse;
import com.montfort.erp.entity.User;
import com.montfort.erp.entity.StudentAccount;
import com.montfort.erp.repository.UserRepository;
import com.montfort.erp.repository.StudentAccountRepository;
import com.montfort.erp.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudentAccountRepository studentAccountRepository;

    public AuthResponse authenticate(AuthRequest request) throws Exception {
        String reqRole = request.getRole();
        String passwordHash = null;
        String actualRole = null;
        String username = null;
        
        if ("Parents".equalsIgnoreCase(reqRole)) {
            StudentAccount account = studentAccountRepository.findByUsernameAndIsActive(request.getUsername(), 1)
                    .orElseThrow(() -> new UsernameNotFoundException("Parent account not found"));
            
            if (request.getBranch_id() != null && !request.getBranch_id().isEmpty()) {
                if (!account.getBranchId().toString().equals(request.getBranch_id())) {
                    throw new Exception("Account not found in selected branch");
                }
            }
            passwordHash = account.getPassword();
            actualRole = account.getRole();
            username = account.getUsername();
        } else {
            User user = userRepository.findByUsernameAndIsActive(request.getUsername(), 1)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
                    
            if (user.getRole() != null && reqRole != null && !user.getRole().equalsIgnoreCase(reqRole)) {
                throw new Exception("User does not have the selected role");
            }
            
            passwordHash = user.getPassword();
            actualRole = user.getRole();
            username = user.getUsername();
        }
        
        // Check password (assuming bcrypt)
        if (passwordEncoder.matches(request.getPassword(), passwordHash)) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
            String token = jwtUtil.generateToken(userDetails);
            return new AuthResponse(token, username, actualRole);
        } else {
            throw new Exception("Invalid credentials");
        }
    }
}
