package com.erp.montfortuganda.auth.service;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.io.Serial;
import java.util.Collection;

/**
 * Authenticated Spring Security principal containing the ERP user and branch
 * identifiers required by normal request processing.
 *
 * <p>Keeping these immutable values in the authenticated principal avoids
 * executing a second user/branch database lookup in every protected API
 * request. Authentication itself remains DB-backed in UserDetailsServiceImpl,
 * so account status and roles are still validated when the JWT is accepted.</p>
 */
@Getter
public final class AuthenticatedUserPrincipal extends User {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Integer userId;
    private final Integer branchId;
    private final String branchName;
    private final String schoolCode;

    public AuthenticatedUserPrincipal(
            Integer userId,
            String username,
            String password,
            boolean enabled,
            Collection<? extends GrantedAuthority> authorities,
            Integer branchId,
            String branchName,
            String schoolCode
    ) {
        super(
                username,
                password,
                enabled,
                true,
                true,
                true,
                authorities
        );

        if (userId == null) {
            throw new IllegalArgumentException(
                    "Authenticated user ID cannot be null."
            );
        }

        this.userId = userId;
        this.branchId = branchId;
        this.branchName = branchName;
        this.schoolCode = schoolCode;
    }
}
