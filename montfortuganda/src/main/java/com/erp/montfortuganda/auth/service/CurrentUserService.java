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
public class CurrentUserService {

    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public CurrentUserContext getCurrentUserContext() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        return getCurrentUserContext(authentication);
    }

    public CurrentUserContext getCurrentUserContext(
            Authentication authentication
    ) {

        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {

            throw new AuthenticationCredentialsNotFoundException(
                    "No authenticated user found"
            );
        }

        String username = authentication.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new AuthenticationCredentialsNotFoundException(
                                "Authenticated user not found: " + username
                        )
                );

        CurrentUserContext ctx = new CurrentUserContext();

        ctx.setUserId(user.getId());
        ctx.setUsername(user.getUsername());

        List<String> roles = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .distinct()
                .toList();

        ctx.setRoles(roles);

        Branch branch = user.getAssignedBranch();

        if (branch != null) {
            ctx.setBranchId(branch.getBranchId());
            ctx.setBranchName(branch.getBranchName());
            ctx.setSchoolCode(branch.getSchoolCode());
        } else {
            ctx.setBranchId(null);
            ctx.setBranchName(null);
            ctx.setSchoolCode(null);
        }

        return ctx;
    }
}