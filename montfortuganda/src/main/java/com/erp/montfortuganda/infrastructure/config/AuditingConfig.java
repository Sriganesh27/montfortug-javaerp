package com.erp.montfortuganda.infrastructure.config;

import com.erp.montfortuganda.auth.service.AuthenticatedUserPrincipal;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class AuditingConfig {

    @Bean
    public AuditorAware<String> auditorProvider() {

        return () -> {

            Authentication authentication =
                    SecurityContextHolder
                            .getContext()
                            .getAuthentication();

            if (authentication == null) {
                return Optional.of("system");
            }

            if (!authentication.isAuthenticated()) {
                return Optional.of("system");
            }

            if (authentication
                    instanceof AnonymousAuthenticationToken) {
                return Optional.of("system");
            }

            Object principal =
                    authentication.getPrincipal();

            if (principal
                    instanceof AuthenticatedUserPrincipal
                    authenticatedUser) {

                return Optional.of(
                        String.valueOf(
                                authenticatedUser.getUserId()
                        )
                );
            }

            /*
             * Safe fallback for JWT filters or tests that may temporarily
             * create a standard Spring Security principal.
             */
            String username =
                    authentication.getName();

            if (username == null
                    || username.isBlank()
                    || "anonymousUser".equalsIgnoreCase(username)) {

                return Optional.of("system");
            }

            return Optional.of(username);
        };
    }
}