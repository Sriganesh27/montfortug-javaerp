package com.erp.montfortuganda.auth.service;

import com.erp.montfortuganda.auth.User;
import com.erp.montfortuganda.auth.repository.UserRepository;
import com.erp.montfortuganda.exception.ResourceNotFoundException;
import com.erp.montfortuganda.exception.UnauthorizedException;
import org.springframework.stereotype.Service;
import java.security.Principal;

@Service
public class CurrentUserService {

    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Now returns the clean Context instead of the JPA Entity!
    public CurrentUserContext getCurrentUserContext(Principal principal) {
        if (principal == null || principal.getName() == null) {
            throw new UnauthorizedException("User session is invalid or not authenticated.");
        }

        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found for session: " + principal.getName()));

        CurrentUserContext ctx = new CurrentUserContext();
        ctx.setUserId(user.getId());
        ctx.setUsername(user.getUsername());

        if (user.getAssignedBranch() != null) {
            ctx.setBranchId(user.getAssignedBranch().getBranchId());
            ctx.setBranchName(user.getAssignedBranch().getBranchName());
        }

        return ctx;
    }
}