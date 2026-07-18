package com.erp.montfortuganda.infrastructure.service;

import com.erp.montfortuganda.infrastructure.enums.DocumentType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.UUID;

@Service
public class StorageService {

    /**
     * Public portal uploads.
     *
     * Example:
     * uploads/applications/APP-2026-001/photo.jpg
     */
    @Value("${erp.storage.location:uploads}")
    private String publicUploadRoot;

    /**
     * Private ERP employee/student storage.
     *
     * Example:
     * erp-storage/branchdetails/U011-School Name,Location/staff/
     */
    @Value("${erp.storage.private-location:erp-storage}")
    private String privateErpStorageRoot;

    /**
     * Existing public portal storage method.
     *
     * Keep this method for application portal files and any existing modules
     * already using storeEntityDocument().
     */
    public String storeEntityDocument(
            MultipartFile file,
            String schoolPath,
            String modulePath,
            String entityPath,
            DocumentType type
    ) {
        validateFile(file);

        if (isBlank(schoolPath)
                || isBlank(modulePath)
                || isBlank(entityPath)
                || type == null) {
            throw new IllegalArgumentException(
                    "Invalid public storage hierarchy parameters"
            );
        }

        try {
            Path rootPath =
                    Paths.get(publicUploadRoot)
                            .normalize()
                            .toAbsolutePath();

            Path targetDirectory =
                    rootPath
                            .resolve(sanitizePathSegment(schoolPath))
                            .resolve(sanitizePathSegment(modulePath))
                            .resolve(sanitizePathSegment(entityPath))
                            .normalize();

            validateTargetDirectory(
                    rootPath,
                    targetDirectory
            );

            Files.createDirectories(
                    targetDirectory
            );

            String extension =
                    getSafeExtension(
                            file.getOriginalFilename()
                    );

            String newFilename =
                    sanitizeFilePrefix(type.name())
                            + "_"
                            + System.currentTimeMillis()
                            + "_"
                            + generateRandomSuffix()
                            + extension;

            Path targetFile =
                    targetDirectory
                            .resolve(newFilename)
                            .normalize();

            validateTargetFile(
                    targetDirectory,
                    targetFile
            );

            Files.copy(
                    file.getInputStream(),
                    targetFile,
                    StandardCopyOption.REPLACE_EXISTING
            );

            return toRelativePath(
                    rootPath,
                    targetFile
            );

        } catch (IOException exception) {
            throw new RuntimeException(
                    "Could not store public document",
                    exception
            );
        }
    }

    /**
     * Stores private ERP documents outside the public portal uploads folder.
     *
     * Final hierarchy:
     *
     * erp-storage/
     *   branchdetails/
     *     U011-School Name,Location/
     *       staff/
     *         U011-T-2026-0001-John Doe/
     *           qualifications/
     *             graduation_timestamp_random.pdf
     *           experience/
     *             experience_company_timestamp_random.pdf
     *           documents/
     *             national_id_timestamp_random.pdf
     *
     * Student hierarchy:
     *
     * erp-storage/
     *   branchdetails/
     *     U011-School Name,Location/
     *       student/
     *         STUDENT-NO-Student Name/
     *           documents/
     */
    public String storePrivateEntityDocument(
            MultipartFile file,
            String branchPath,
            String entityType,
            String uniqueEntityPath,
            String documentGroup,
            String filePrefix
    ) {
        validateFile(file);

        if (isBlank(branchPath)) {
            throw new IllegalArgumentException(
                    "Branch storage path is required"
            );
        }

        if (isBlank(entityType)) {
            throw new IllegalArgumentException(
                    "Entity type is required"
            );
        }

        if (isBlank(uniqueEntityPath)) {
            throw new IllegalArgumentException(
                    "Unique employee or student path is required"
            );
        }

        if (isBlank(documentGroup)) {
            throw new IllegalArgumentException(
                    "Document group is required"
            );
        }

        if (isBlank(filePrefix)) {
            throw new IllegalArgumentException(
                    "File prefix is required"
            );
        }

        try {
            Path rootPath =
                    Paths.get(privateErpStorageRoot)
                            .normalize()
                            .toAbsolutePath();

            String safeBranchPath =
                    sanitizePathSegment(branchPath);

            String safeEntityType =
                    sanitizePathSegment(entityType)
                            .toLowerCase(Locale.ROOT);

            String safeUniqueEntityPath =
                    sanitizePathSegment(uniqueEntityPath);

            String safeDocumentGroup =
                    sanitizePathSegment(documentGroup)
                            .toLowerCase(Locale.ROOT);

            Path targetDirectory =
                    rootPath
                            .resolve("branchdetails")
                            .resolve(safeBranchPath)
                            .resolve(safeEntityType)
                            .resolve(safeUniqueEntityPath)
                            .resolve(safeDocumentGroup)
                            .normalize();

            validateTargetDirectory(
                    rootPath,
                    targetDirectory
            );

            Files.createDirectories(
                    targetDirectory
            );

            String extension =
                    getSafeExtension(
                            file.getOriginalFilename()
                    );

            String storedFileName =
                    sanitizeFilePrefix(filePrefix)
                            + "_"
                            + System.currentTimeMillis()
                            + "_"
                            + generateRandomSuffix()
                            + extension;

            Path targetFile =
                    targetDirectory
                            .resolve(storedFileName)
                            .normalize();

            validateTargetFile(
                    targetDirectory,
                    targetFile
            );

            Files.copy(
                    file.getInputStream(),
                    targetFile,
                    StandardCopyOption.REPLACE_EXISTING
            );

            return toRelativePath(
                    rootPath,
                    targetFile
            );

        } catch (IOException exception) {
            throw new RuntimeException(
                    "Could not store private ERP document",
                    exception
            );
        }
    }

    private void validateFile(
            MultipartFile file
    ) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "Cannot store an empty file"
            );
        }
    }

    private void validateTargetDirectory(
            Path rootPath,
            Path targetDirectory
    ) {
        if (!targetDirectory.startsWith(rootPath)) {
            throw new SecurityException(
                    "Path traversal attempt detected"
            );
        }
    }

    private void validateTargetFile(
            Path targetDirectory,
            Path targetFile
    ) {
        if (!targetFile.startsWith(targetDirectory)) {
            throw new SecurityException(
                    "Invalid target file path"
            );
        }
    }

    /**
     * Allows readable folder names such as:
     *
     * U011-St Montfort School,Mpala
     * U011-T-2026-0001-John Doe
     */
    private String sanitizePathSegment(
            String input
    ) {
        if (isBlank(input)) {
            return "unknown";
        }

        String sanitized =
                input.trim()
                        .replaceAll(
                                "[^a-zA-Z0-9.\\- ,_]",
                                "_"
                        )
                        .replaceAll(
                                "\\s+",
                                " "
                        );

        if (sanitized.length() > 180) {
            sanitized =
                    sanitized.substring(0, 180);
        }

        return sanitized;
    }

    /**
     * Converts:
     *
     * GRADUATION -> graduation
     * NATIONAL_ID -> national_id
     * EXPERIENCE_MAKERERE -> experience_makerere
     */
    private String sanitizeFilePrefix(
            String input
    ) {
        if (isBlank(input)) {
            return "document";
        }

        String sanitized =
                input.trim()
                        .toLowerCase(Locale.ROOT)
                        .replaceAll(
                                "[^a-z0-9._-]",
                                "_"
                        )
                        .replaceAll(
                                "_+",
                                "_"
                        )
                        .replaceAll(
                                "^_+|_+$",
                                ""
                        );

        if (sanitized.isBlank()) {
            sanitized = "document";
        }

        if (sanitized.length() > 80) {
            sanitized =
                    sanitized.substring(0, 80);
        }

        return sanitized;
    }

    private String generateRandomSuffix() {
        return UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 8);
    }

    private String getSafeExtension(
            String originalFilename
    ) {
        if (originalFilename == null) {
            return ".bin";
        }

        String lower =
                originalFilename.toLowerCase(
                        Locale.ROOT
                );

        if (lower.endsWith(".jpg")) {
            return ".jpg";
        }

        if (lower.endsWith(".jpeg")) {
            return ".jpeg";
        }

        if (lower.endsWith(".png")) {
            return ".png";
        }

        if (lower.endsWith(".pdf")) {
            return ".pdf";
        }

        return ".bin";
    }

    private String toRelativePath(
            Path rootPath,
            Path targetFile
    ) {
        return rootPath
                .relativize(targetFile)
                .toString()
                .replace("\\", "/");
    }

    private boolean isBlank(
            String value
    ) {
        return value == null || value.isBlank();
    }
}