package com.erp.montfortuganda.school.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.erp.montfortuganda.school.service.FileStorageService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final String BRANCH_DIRECTORY = "branches";

    private final Path privateStorageRoot;

    public FileStorageService(
            @Value(
                    "${erp.storage.private-location:erp_storage}"
            )
            String privateStorageLocation
    ) {
        this.privateStorageRoot =
                Path.of(privateStorageLocation)
                        .toAbsolutePath()
                        .normalize();
    }

    @PostConstruct
    public void initializeStorage() {
        try {
            Files.createDirectories(privateStorageRoot);
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Could not initialize private ERP storage.",
                    exception
            );
        }
    }

    public String saveBranchLogo(
            Integer branchId,
            String schoolCode,
            String branchName,
            String branchLocation,
            MultipartFile logo
    ) {
        return saveBranchFile(
                branchId,
                schoolCode,
                branchName,
                branchLocation,
                "logo",
                "branch-logo",
                logo
        );
    }

    public String saveBranchPhoto(
            Integer branchId,
            String schoolCode,
            String branchName,
            String branchLocation,
            MultipartFile photo
    ) {
        return saveBranchFile(
                branchId,
                schoolCode,
                branchName,
                branchLocation,
                "photo",
                "school-photo",
                photo
        );
    }

    public List<String> saveBranchDocuments(
            Integer branchId,
            String schoolCode,
            String branchName,
            String branchLocation,
            List<MultipartFile> documents
    ) {
        List<String> storedPaths = new ArrayList<>();

        if (documents == null || documents.isEmpty()) {
            return storedPaths;
        }

        for (MultipartFile document : documents) {
            if (document == null || document.isEmpty()) {
                continue;
            }

            String storedPath = saveBranchFile(
                    branchId,
                    schoolCode,
                    branchName,
                    branchLocation,
                    "documents",
                    "government-document",
                    document
            );

            if (storedPath != null) {
                storedPaths.add(storedPath);
            }
        }

        return storedPaths;
    }

    public Resource loadPrivateFile(
            String relativePath
    ) {
        String normalizedRelativePath =
                normalizeRelativePath(relativePath);

        Path filePath =
                privateStorageRoot
                        .resolve(normalizedRelativePath)
                        .normalize();

        ensureInsideStorage(filePath);

        try {
            Resource resource =
                    new UrlResource(filePath.toUri());

            if (
                    !resource.exists()
                            || !resource.isReadable()
                            || !Files.isRegularFile(filePath)
            ) {
                throw new IllegalArgumentException(
                        "Requested file does not exist."
                );
            }

            return resource;
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Could not read the requested private file.",
                    exception
            );
        }
    }

    public String detectContentType(
            String relativePath
    ) {
        String normalizedRelativePath =
                normalizeRelativePath(relativePath);

        Path filePath =
                privateStorageRoot
                        .resolve(normalizedRelativePath)
                        .normalize();

        ensureInsideStorage(filePath);

        try {
            String detectedType =
                    Files.probeContentType(filePath);

            return detectedType == null
                    ? "application/octet-stream"
                    : detectedType;
        } catch (IOException exception) {
            return "application/octet-stream";
        }
    }

    public void deletePrivateFile(
            String relativePath
    ) {
        if (
                relativePath == null
                        || relativePath.isBlank()
        ) {
            return;
        }

        String normalizedRelativePath =
                normalizeRelativePath(relativePath);

        Path filePath =
                privateStorageRoot
                        .resolve(normalizedRelativePath)
                        .normalize();

        ensureInsideStorage(filePath);

        try {
            Files.deleteIfExists(filePath);
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Could not delete the private file.",
                    exception
            );
        }
    }

    private String saveBranchFile(
            Integer branchId,
            String schoolCode,
            String branchName,
            String branchLocation,
            String fileCategory,
            String filePrefix,
            MultipartFile file
    ) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        validateBranchIdentity(
                branchId,
                schoolCode,
                branchName,
                branchLocation
        );

        String branchFolderName =
                buildBranchFolderName(
                        branchId,
                        schoolCode,
                        branchName,
                        branchLocation
                );

        Path categoryDirectory =
                privateStorageRoot
                        .resolve(BRANCH_DIRECTORY)
                        .resolve(branchFolderName)
                        .resolve(sanitizePathSegment(fileCategory))
                        .normalize();

        ensureInsideStorage(categoryDirectory);

        String extension =
                extractSafeExtension(
                        file.getOriginalFilename()
                );

        String storedFilename =
                sanitizePathSegment(filePrefix)
                        + "-"
                        + UUID.randomUUID()
                        + extension;

        Path targetFile =
                categoryDirectory
                        .resolve(storedFilename)
                        .normalize();

        ensureInsideStorage(targetFile);

        try {
            Files.createDirectories(categoryDirectory);

            try (var inputStream = file.getInputStream()) {
                Files.copy(
                        inputStream,
                        targetFile,
                        StandardCopyOption.REPLACE_EXISTING
                );
            }

            return privateStorageRoot
                    .relativize(targetFile)
                    .toString()
                    .replace('\\', '/');
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Failed to store the private branch file.",
                    exception
            );
        }
    }

    private String buildBranchFolderName(
            Integer branchId,
            String schoolCode,
            String branchName,
            String branchLocation
    ) {
        /*
         * branchId provides immutable uniqueness.
         * The remaining values make the folder readable.
         *
         * Example:
         * 3-U031-Mpala-Primary-School-Mpala-Entebbe
         */
        return branchId
                + "-"
                + sanitizePathSegment(schoolCode)
                + "-"
                + sanitizePathSegment(branchName)
                + "-"
                + sanitizePathSegment(branchLocation);
    }

    private void validateBranchIdentity(
            Integer branchId,
            String schoolCode,
            String branchName,
            String branchLocation
    ) {
        if (branchId == null || branchId <= 0) {
            throw new IllegalArgumentException(
                    "A saved branch ID is required before storing files."
            );
        }

        requireText(schoolCode, "School code");
        requireText(branchName, "Branch name");
        requireText(branchLocation, "Branch location");
    }

    private void requireText(
            String value,
            String fieldName
    ) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    fieldName + " is required for private file storage."
            );
        }
    }

    private String sanitizePathSegment(
            String value
    ) {
        if (value == null || value.isBlank()) {
            return "unknown";
        }

        String sanitized =
                value.trim()
                        .replaceAll("[^A-Za-z0-9_-]+", "-")
                        .replaceAll("-{2,}", "-")
                        .replaceAll("^-|-$", "");

        return sanitized.isBlank()
                ? "unknown"
                : sanitized;
    }

    private String extractSafeExtension(
            String originalFilename
    ) {
        if (
                originalFilename == null
                        || originalFilename.isBlank()
        ) {
            return "";
        }

        String filename =
                Path.of(originalFilename)
                        .getFileName()
                        .toString();

        int extensionIndex =
                filename.lastIndexOf('.');

        if (
                extensionIndex < 0
                        || extensionIndex
                        == filename.length() - 1
        ) {
            return "";
        }

        String extension =
                filename.substring(extensionIndex + 1)
                        .toLowerCase(Locale.ROOT)
                        .replaceAll("[^a-z0-9]", "");

        if (extension.isBlank() || extension.length() > 10) {
            return "";
        }

        return "." + extension;
    }

    private String normalizeRelativePath(
            String relativePath
    ) {
        if (
                relativePath == null
                        || relativePath.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "A relative private file path is required."
            );
        }

        Path normalizedPath =
                Path.of(relativePath)
                        .normalize();

        if (
                normalizedPath.isAbsolute()
                        || normalizedPath.startsWith("..")
        ) {
            throw new SecurityException(
                    "Invalid private file path."
            );
        }

        return normalizedPath
                .toString()
                .replace('\\', '/');
    }

    private void ensureInsideStorage(
            Path path
    ) {
        if (!path.normalize().startsWith(privateStorageRoot)) {
            throw new SecurityException(
                    "Private storage path traversal was blocked."
            );
        }
    }
}