package com.erp.montfortuganda.auth.config;

import com.erp.montfortuganda.auth.entity.User;
import com.erp.montfortuganda.auth.repository.UserRepository;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
@ConditionalOnProperty(
        name = "erp.bootstrap-admin.enabled",
        havingValue = "true"
)
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String bootstrapUsername;
    private final String bootstrapPassword;

    public DatabaseSeeder(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${erp.bootstrap-admin.username:}")
            String bootstrapUsername,
            @Value("${erp.bootstrap-admin.password:}")
            String bootstrapPassword
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.bootstrapUsername = bootstrapUsername;
        this.bootstrapPassword = bootstrapPassword;
    }

    @Override
    public void run(String @NonNull ... args) {
        if (userRepository.count() != 0) {
            return;
        }

        validateBootstrapCredentials();

        User admin = new User();
        admin.setUsername(bootstrapUsername.trim());
        admin.setPassword(
                passwordEncoder.encode(bootstrapPassword)
        );
        admin.setRole("SUPER_ADMIN");
        admin.setIsActive(1);

        userRepository.save(admin);
    }

    private void validateBootstrapCredentials() {
        if (
                bootstrapUsername == null
                        || bootstrapUsername.isBlank()
        ) {
            throw new IllegalStateException(
                    "ERP_BOOTSTRAP_ADMIN_USERNAME is required "
                            + "when development bootstrap is enabled."
            );
        }

        if (
                bootstrapPassword == null
                        || bootstrapPassword.length() < 16
        ) {
            throw new IllegalStateException(
                    "ERP_BOOTSTRAP_ADMIN_PASSWORD must contain "
                            + "at least 16 characters."
            );
        }
    }
}
