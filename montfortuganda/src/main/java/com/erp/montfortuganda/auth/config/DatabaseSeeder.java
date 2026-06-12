package com.erp.montfortuganda.auth.config;

import com.erp.montfortuganda.auth.User;
import com.erp.montfortuganda.auth.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // Check if there are any users in the database
        if (userRepository.count() == 0) {

            System.out.println("No users found. Creating default Super Admin...");

            User admin = new User();
            admin.setUsername("admin");
            // BCrypt will securely encrypt this password before saving to the VPS
            admin.setPassword(passwordEncoder.encode("password123"));
            admin.setRole("SUPER_ADMIN");
            admin.setIsActive(1);
            // Notice we do not set a Branch. A Super Admin has access to ALL branches.

            userRepository.save(admin);

            System.out.println("Super Admin created successfully! Username: admin | Password: password123");
        }
    }
}