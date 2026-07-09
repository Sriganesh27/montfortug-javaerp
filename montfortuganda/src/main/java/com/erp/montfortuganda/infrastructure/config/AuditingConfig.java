package com.erp.montfortuganda.infrastructure.config;

import com.erp.montfortuganda.auth.service.CurrentUserContext;
import com.erp.montfortuganda.auth.service.CurrentUserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class AuditingConfig {

    private final CurrentUserService currentUserService;

    public AuditingConfig(CurrentUserService currentUserService) {
        this.currentUserService = currentUserService;
    }

    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
                return Optional.empty();
            }
            try {
                CurrentUserContext ctx = currentUserService.getCurrentUserContext(authentication);
                if (ctx != null && ctx.getUserId() != null) {
                    return Optional.of(String.valueOf(ctx.getUserId()));
                }
                return Optional.of("system");
            } catch (Exception e) {
                return Optional.empty();
            }
        };
    }
}