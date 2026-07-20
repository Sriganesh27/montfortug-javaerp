package com.erp.montfortuganda.auth.service;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.io.Serial;
import java.util.Collection;

/**
 * Authenticated Spring Security principal containing the ERP user ID.
 *
 * This allows JPA auditing to obtain the authenticated user's ID directly
 * without executing another database query during Hibernate flush.
 */
@Getter
public final class AuthenticatedUserPrincipal extends User {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Integer userId;

    public AuthenticatedUserPrincipal(
            Integer userId,
            String username,
            String password,
            boolean enabled,
            Collection<? extends GrantedAuthority> authorities
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
    }

}