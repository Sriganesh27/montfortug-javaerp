package com.erp.montfortuganda.auth.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CurrentUserService {

    /**
     * [NEW] Ambient context resolver!
     * This allows deeply decoupled services to get the current user without touching Spring Security context directly.
     */
    public CurrentUserContext getCurrentUserContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return getCurrentUserContext(authentication);
    }

    /**
     * Extracts the custom context from Spring Security Authentication.
     * Hardcoded for Phase 1 testing until full JWT is implemented in Phase 2/3.
     */
    public CurrentUserContext getCurrentUserContext(Authentication authentication) {
        CurrentUserContext ctx = new CurrentUserContext();
        ctx.setUserId(1); // Hardcoded Super Admin for testing
        ctx.setUsername("admin@system");
        ctx.setRoles(List.of("SUPER_ADMIN"));
        ctx.setSchoolId(1L);
        ctx.setBranchId(1);
        ctx.setSchoolCode("SYS");

        return ctx;
    }
}