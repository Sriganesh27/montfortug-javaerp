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

        if (username == null || username.isBlank()) {
            throw new UsernameNotFoundException(
                    "Username is required."
            );
        }

        User user = userRepository.findByUsername(username.trim())
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "User not found: " + username
                        )
                );

        List<GrantedAuthority> authorities =
                resolveAuthorities(user);

        if (authorities.isEmpty()) {
            throw new UsernameNotFoundException(
                    "No active role assigned to user: "
                            + user.getUsername()
            );
        }

        boolean enabled =
                Integer.valueOf(1).equals(user.getIsActive());

        return new AuthenticatedUserPrincipal(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                enabled,
                authorities
        );
    }

    private List<GrantedAuthority> resolveAuthorities(User user) {

        List<GrantedAuthority> authorities =
                new ArrayList<>();

        /*
         * Primary dynamic RBAC role mapping.
         */
        if (user.getUserRoles() != null) {

            for (ErpUserRole userRole : user.getUserRoles()) {

                if (userRole == null) {
                    continue;
                }

                if (!Boolean.TRUE.equals(userRole.getActive())) {
                    continue;
                }

                if (userRole.getRole() == null) {
                    continue;
                }

                if (!Boolean.TRUE.equals(
                        userRole.getRole().getActive()
                )) {
                    continue;
                }

                String roleCode = normalizeRoleCode(
                        userRole.getRole().getRoleCode()
                );

                if (roleCode == null) {
                    continue;
                }

                String authorityName =
                        "ROLE_" + roleCode;

                boolean alreadyAdded =
                        authorities.stream()
                                .anyMatch(authority ->
                                        authority.getAuthority()
                                                .equals(authorityName)
                                );

                if (!alreadyAdded) {
                    authorities.add(
                            new SimpleGrantedAuthority(
                                    authorityName
                            )
                    );
                }
            }
        }

        /*
         * Legacy erp_users.role fallback.
         *
         * This remains for users that have not yet been migrated
         * to erp_user_roles.
         */
        if (authorities.isEmpty()) {

            String legacyRole =
                    normalizeRoleCode(user.getRole());

            if (legacyRole != null) {
                authorities.add(
                        new SimpleGrantedAuthority(
                                "ROLE_" + legacyRole
                        )
                );
            }
        }

        return authorities;
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