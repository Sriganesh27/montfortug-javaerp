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

    private final String BASE_DIR = "uploads/branchdetails";

    public String saveFile(String code, String name, String loc, String type, MultipartFile file) {
        if (file == null || file.isEmpty()) return null;

        // Ensure file is < 100KB (102400 bytes)
        if (file.getSize() > 102400) {
            throw new RuntimeException("File exceeds maximum allowed size of 100KB");
        }

        try {
            // Creates: uploads/branchdetails/CODE-NAME-LOCATION/school_documents/
            String folder = sanitize(code) + "-" + sanitize(name) + "-" + sanitize(loc);
            Path path = Paths.get(BASE_DIR, folder, type);
            if (!Files.exists(path)) Files.createDirectories(path);

            String fileName = System.currentTimeMillis() + "_" + sanitize(file.getOriginalFilename());
            Path filePath = path.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Return web URL path
            return "/" + BASE_DIR + "/" + folder + "/" + type + "/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }
    }

    private String sanitize(String input) {
        if (input == null) return "unknown";
        return input.replaceAll("[^a-zA-Z0-9\\.\\-]", "_");
    }
}