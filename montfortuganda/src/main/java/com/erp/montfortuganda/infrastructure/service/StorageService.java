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
    // Example: uploads/staff/school_1/branch_5/employee_15/photo.jpg
    public String storeEntityDocument(MultipartFile file, Long schoolId, Long branchId,
                                      String moduleFolderName, String entityPrefix, Long entityDbId, DocumentType type) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Cannot store empty file");
        }

        if (schoolId == null || branchId == null || entityDbId == null) {
            throw new IllegalArgumentException("Invalid storage hierarchy parameters");
        }

        try {
            String safeModule = sanitize(moduleFolderName); // e.g. "staff"
            String safePrefix = sanitize(entityPrefix); // e.g. "employee"

            String relativePath = safeModule + File.separator
                    + "school_" + schoolId + File.separator
                    + "branch_" + branchId + File.separator
                    + safePrefix + "_" + entityDbId + File.separator;

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

    private String sanitize(String input) {
        if (input == null || input.trim().isEmpty()) return "unknown";
        return input.trim().replaceAll("[^a-zA-Z0-9.\\-]", "_");
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