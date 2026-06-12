package com.montfort.erp;

import com.montfort.erp.entities.User;
import com.montfort.erp.repositories.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class ErpApplication {

    public static void main(String[] args) {
        SpringApplication.run(ErpApplication.class, args);
    }

    @Bean
    public CommandLineRunner initSuperAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // Check if the user already exists
            if (userRepository.findByUsername("erpsadmin").isEmpty()) {
                User superAdmin = new User();
                superAdmin.setUsername("erpsadmin");
                // Hash the password automatically using BCrypt
                superAdmin.setPassword(passwordEncoder.encode("montfort@uganda2022"));
                superAdmin.setRole("Super User");
                superAdmin.setIsActive(1);
                
                userRepository.save(superAdmin);
                System.out.println("\n=========================================");
                System.out.println("SUCCESS: Super Admin 'erpsadmin' created!");
                System.out.println("=========================================\n");
            }
        };
    }
}
