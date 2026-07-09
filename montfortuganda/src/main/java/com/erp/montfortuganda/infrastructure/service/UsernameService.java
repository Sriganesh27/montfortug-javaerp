package com.erp.montfortuganda.infrastructure.service;

import com.erp.montfortuganda.auth.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UsernameService {

    private final UserRepository userRepository;

    public UsernameService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public String generateUniqueUsername(String firstName, String lastName, String schoolCode, java.time.LocalDate joiningDate) {
        String baseName = (firstName.trim() + "." + lastName.trim()).toLowerCase().replaceAll("[^a-z0-9.]", "");
        String safeSchoolCode = schoolCode != null ? schoolCode.trim().toLowerCase() : "sys";
        String yy = joiningDate != null ? String.format("%02d", joiningDate.getYear() % 100) : "00";

        String suffix = "_" + safeSchoolCode + yy;
        String username = baseName + suffix;

        int counter = 1;
        while (userRepository.existsByUsername(username)) {
            username = baseName + String.format("%02d", counter) + suffix;
            counter++;
        }
        return username;
    }
}