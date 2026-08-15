package com.erp.montfortuganda.auth.service;

import com.erp.montfortuganda.auth.entity.User;
import com.erp.montfortuganda.auth.repository.UserRepository;
import com.erp.montfortuganda.school.entity.Branch;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CurrentUserService {

    private final UserRepository userRepository;

    public CurrentUserService(
            UserRepository userRepository
    ) {
        this.userRepository = userRepository;
    }

    public CurrentUserContext getCurrentUserContext() {
        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        return getCurrentUserContext(authentication);
    }

    public CurrentUserContext getCurrentUserContext(
            Authentication authentication
    ) {
        requireAuthentication(authentication);

        /*
         * Normal JWT-authenticated request path.
         *
         * UserDetailsServiceImpl has already loaded the user, active roles,
         * and assigned branch while authenticating this request. Reuse those
         * immutable principal values instead of immediately repeating the
         * same user/branch query.
         */
        if (
                authentication.getPrincipal()
                        instanceof AuthenticatedUserPrincipal principal
        ) {
            return fromAuthenticatedPrincipal(
                    authentication,
                    principal
            );
        }

        /*
         * Compatibility fallback for any non-JWT/legacy authentication
         * provider that still supplies a standard Spring principal.
         */
        return loadLegacyContext(
                authentication
        );
    }

    private CurrentUserContext fromAuthenticatedPrincipal(
            Authentication authentication,
            AuthenticatedUserPrincipal principal
    ) {
        CurrentUserContext context =
                new CurrentUserContext();

        context.setUserId(
                principal.getUserId()
        );

        context.setUsername(
                principal.getUsername()
        );

        context.setRoles(
                resolveRoles(authentication)
        );

        context.setBranchId(
                principal.getBranchId()
        );

        context.setBranchName(
                principal.getBranchName()
        );

        context.setSchoolCode(
                principal.getSchoolCode()
        );

        return context;
    }

    private CurrentUserContext loadLegacyContext(
            Authentication authentication
    ) {
        String username =
                authentication.getName();

        User user =
                userRepository
                        .findByUsernameWithAssignedBranch(
                                username
                        )
                        .orElseThrow(
                                () ->
                                        new AuthenticationCredentialsNotFoundException(
                                                "Authenticated user not found: "
                                                        + username
                                        )
                        );

        CurrentUserContext context =
                new CurrentUserContext();

        context.setUserId(
                user.getId()
        );

        context.setUsername(
                user.getUsername()
        );

        context.setRoles(
                resolveRoles(authentication)
        );

        Branch branch =
                user.getAssignedBranch();

        if (branch != null) {
            context.setBranchId(
                    branch.getBranchId()
            );

            context.setBranchName(
                    branch.getBranchName()
            );

            context.setSchoolCode(
                    branch.getSchoolCode()
            );
        } else {
            context.setBranchId(null);
            context.setBranchName(null);
            context.setSchoolCode(null);
        }

        return context;
    }

    private List<String> resolveRoles(
            Authentication authentication
    ) {
        return authentication
                .getAuthorities()
                .stream()
                .map(
                        GrantedAuthority::getAuthority
                )
                .distinct()
                .toList();
    }

    private void requireAuthentication(
            Authentication authentication
    ) {
        if (
                authentication == null
                        || !authentication.isAuthenticated()
                        || "anonymousUser".equals(
                        authentication.getPrincipal()
                )
        ) {
            throw new AuthenticationCredentialsNotFoundException(
                    "No authenticated user found"
            );
        }
    }
}
