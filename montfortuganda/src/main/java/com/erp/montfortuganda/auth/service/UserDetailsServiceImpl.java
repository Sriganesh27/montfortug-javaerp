package com.erp.montfortuganda.auth.service;

import com.erp.montfortuganda.auth.entity.ErpUserRole;
import com.erp.montfortuganda.auth.entity.User;
import com.erp.montfortuganda.auth.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "User not found: " + username
                        )
                );

        List<GrantedAuthority> authorities = new ArrayList<>();

        /*
         * New dynamic RBAC roles.
         */
        if (user.getUserRoles() != null) {
            for (ErpUserRole userRole : user.getUserRoles()) {

                if (userRole == null
                        || !Boolean.TRUE.equals(userRole.getActive())
                        || userRole.getRole() == null
                        || !Boolean.TRUE.equals(userRole.getRole().getActive())) {
                    continue;
                }

                String roleCode = normalizeRoleCode(
                        userRole.getRole().getRoleCode()
                );

                if (roleCode != null) {
                    authorities.add(
                            new SimpleGrantedAuthority("ROLE_" + roleCode)
                    );
                }
            }
        }

        /*
         * Legacy erp_users.role fallback.
         */
        if (authorities.isEmpty()) {
            String roleCode = normalizeRoleCode(user.getRole());

            if (roleCode != null) {
                authorities.add(
                        new SimpleGrantedAuthority("ROLE_" + roleCode)
                );
            }
        }

        if (authorities.isEmpty()) {
            throw new UsernameNotFoundException(
                    "No active role assigned to user: " + username
            );
        }

        boolean active = Integer.valueOf(1).equals(user.getIsActive());

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities(authorities)
                .disabled(!active)
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .build();
    }

    private String normalizeRoleCode(String role) {
        if (role == null || role.isBlank()) {
            return null;
        }

        String normalized = role.trim()
                .toUpperCase(Locale.ROOT)
                .replace(' ', '_')
                .replace('-', '_');

        while (normalized.startsWith("ROLE_")) {
            normalized = normalized.substring(5);
        }

        return switch (normalized) {
            case "SUPER_USER" -> "SUPER_ADMIN";
            case "SCHOOL_ADMIN" -> "BRANCH_ADMIN";
            default -> normalized;
        };
    }
}