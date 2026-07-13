package com.erp.montfortuganda.infrastructure.service;

import com.erp.montfortuganda.infrastructure.enums.DocumentType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class StorageService {

    @Value("${erp.storage.location:uploads}")
    private String baseUploadDir;

    // Uses immutable Database IDs instead of volatile names or document numbers
    // Example: uploads/MBSG-Montfort School,Kampala/staff/15-John Doe/photo.jpg
    public String storeEntityDocument(MultipartFile file, String schoolPath, String modulePath, String entityPath, DocumentType type) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Cannot store empty file");
        }

        if (schoolPath == null || modulePath == null || entityPath == null) {
            throw new IllegalArgumentException("Invalid storage hierarchy parameters");
        }

        try {
            // We use sanitize but we allow spaces and commas since the user explicitly requested "schoolname,location" and "id-name"
            String safeSchool = sanitizePathSegment(schoolPath); 
            String safeModule = sanitizePathSegment(modulePath);
            String safeEntity = sanitizePathSegment(entityPath); 

            String relativePath = safeSchool + File.separator
                    + safeModule + File.separator
                    + safeEntity + File.separator;

            Path uploadPath = Paths.get(baseUploadDir).resolve(relativePath).normalize().toAbsolutePath();
            if (!uploadPath.startsWith(Paths.get(baseUploadDir).normalize().toAbsolutePath())) {
                throw new SecurityException("Path traversal attempt detected");
            }

            File dir = uploadPath.toFile();
            if (!dir.exists()) {
                boolean created = dir.mkdirs();
                if (!created && !dir.exists()) {
                    throw new RuntimeException("Failed to create directory: " + dir.getAbsolutePath());
                }
            }

            String safeExt = getSafeExtension(file.getOriginalFilename());
            String newFilename = type.name().toLowerCase() + "_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 6) + safeExt;

            Path targetLocation = uploadPath.resolve(newFilename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return relativePath.replace(File.separator, "/") + newFilename;

        } catch (IOException ex) {
            throw new RuntimeException("Could not store document. Please try again!", ex);
        }
    }

    private String sanitizePathSegment(String input) {
        if (input == null || input.trim().isEmpty()) return "unknown";
        // Allow alphanumeric, space, dot, hyphen, comma, and underscore.
        return input.trim().replaceAll("[^a-zA-Z0-9.\\- ,_]", "_");
    }

    private String getSafeExtension(String originalFilename) {
        if (originalFilename == null) return ".bin";
        String lower = originalFilename.toLowerCase();
        if (lower.endsWith(".jpg")) return ".jpg";
        if (lower.endsWith(".jpeg")) return ".jpeg";
        if (lower.endsWith(".png")) return ".png";
        if (lower.endsWith(".pdf")) return ".pdf";
        return ".bin";
    }
}