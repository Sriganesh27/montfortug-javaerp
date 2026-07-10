package com.erp.montfortuganda.school.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class FileStorageService {

    // Added 'static' to define this correctly as a class constant
    private static final String BASE_DIR = "uploads/branchdetails";

    public String saveFile(String code, String name, String loc, String type, MultipartFile file) {
        if (file == null || file.isEmpty()) return null;

        if (file.getSize() > 10485760) {
            throw new RuntimeException("File exceeds maximum allowed size of 10MB");
        }

        try {
            String folder = sanitize(code) + "-" + sanitize(name) + "-" + sanitize(loc);
            Path basePath = Paths.get(BASE_DIR, folder, type).toAbsolutePath().normalize();
            if (!Files.exists(basePath)) Files.createDirectories(basePath);

            String fileName = System.currentTimeMillis() + "_" + sanitize(file.getOriginalFilename());
            Path filePath = basePath.resolve(fileName).normalize();

            // 🛡️ IDE-Pleasing Strict Path Traversal Check
            if (!filePath.startsWith(basePath)) {
                throw new SecurityException("CRITICAL: Path traversal attempt blocked!");
            }

            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return "/" + BASE_DIR + "/" + folder + "/" + type + "/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }
    }

    private String sanitize(String input) {
        if (input == null) return "unknown";
        // Removed the redundant \\ before the dot!
        return input.replaceAll("[^a-zA-Z0-9.\\-]", "_");
    }
}
