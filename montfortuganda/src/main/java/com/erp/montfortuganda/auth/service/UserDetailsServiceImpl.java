package com.erp.montfortuganda.auth.service;

import com.erp.montfortuganda.auth.ErpUserRole;
import com.erp.montfortuganda.auth.User;
import com.erp.montfortuganda.auth.repository.UserRepository; // NEW IMPORT
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        List<GrantedAuthority> authorities = new ArrayList<>();

        // 1. Dynamic Roles (from erp_user_roles)
        if (user.getUserRoles() != null) {
            for (ErpUserRole userRole : user.getUserRoles()) {
                if (Boolean.TRUE.equals(userRole.getActive()) && Boolean.TRUE.equals(userRole.getRole().getActive())) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + userRole.getRole().getRoleCode()));
                }
            }
        }

        // 2. Legacy Fallback (So live server doesn't crash before SQL runs!)
        if (authorities.isEmpty() && user.getRole() != null && !user.getRole().isEmpty()) {
            String legacyRole = user.getRole().equals("School Admin") ? "BRANCH_ADMIN" : user.getRole();
            authorities.add(new SimpleGrantedAuthority("ROLE_" + legacyRole.toUpperCase().replace(" ", "_")));
        }

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                authorities
        );
    }
}