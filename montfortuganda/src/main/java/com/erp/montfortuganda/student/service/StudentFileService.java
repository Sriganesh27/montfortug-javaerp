package com.erp.montfortuganda.student.service;

import com.erp.montfortuganda.exception.BadRequestException;
import com.erp.montfortuganda.exception.ResourceNotFoundException;
import com.erp.montfortuganda.school.entity.Branch;
import com.erp.montfortuganda.student.entity.ErpStudent;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Secure private-file storage for the Student module.
 *
 * <p>Physical storage paths must never be returned directly to the browser.
 * Controllers must expose files only through protected Student endpoints
 * after validating Student and branch ownership.</p>
 */
@SuppressWarnings("unused")
@Service
public class StudentFileService {

    private static final long ONE_MEGABYTE =
            1024L * 1024L;

    private static final long MAX_PHOTO_BYTES =
            2L * ONE_MEGABYTE;

    private static final long MAX_DOCUMENT_BYTES =
            5L * ONE_MEGABYTE;

    private static final Set<String> IMAGE_MIME_TYPES =
            Set.of(
                    "image/jpeg",
                    "image/png",
                    "image/webp"
            );

    private static final Set<String> DOCUMENT_MIME_TYPES =
            Set.of(
                    "application/pdf",
                    "image/jpeg",
                    "image/png",
                    "image/webp"
            );

    private static final String BRANCH_DETAILS_DIRECTORY =
            "branchdetails";

    private static final String STUDENTS_DIRECTORY =
            "students";

    private final Path privateStorageRoot;

    public StudentFileService(
            @Value(
                    "${erp.storage.private-location:erp-storage}"
            )
            String privateStorageLocation
    ) {
        if (!StringUtils.hasText(privateStorageLocation)) {
            throw new IllegalArgumentException(
                    "Private ERP storage location is required."
            );
        }

        this.privateStorageRoot =
                Path.of(privateStorageLocation)
                        .toAbsolutePath()
                        .normalize();
    }

    @PostConstruct
    public void initializeStorage() {
        try {
            Files.createDirectories(
                    privateStorageRoot
            );

            if (Files.isSymbolicLink(privateStorageRoot)) {
                throw new IllegalStateException(
                        "Private ERP storage root cannot be a symbolic link."
                );
            }
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Could not initialize private Student storage.",
                    exception
            );
        }
    }

    // =====================================================================
    // STUDENT PHOTO
    // =====================================================================

    public StoredFile storeStudentPhoto(
            MultipartFile photo,
            ErpStudent student
    ) {
        return storeMultipartFile(
                photo,
                student,
                "profile",
                "student-photo",
                MAX_PHOTO_BYTES,
                IMAGE_MIME_TYPES
        );
    }

    public StoredFile replaceStudentPhoto(
            MultipartFile photo,
            ErpStudent student,
            String existingRelativePath
    ) {
        StoredFile storedFile =
                storeStudentPhoto(
                        photo,
                        student
                );

        scheduleDeleteAfterCommit(
                existingRelativePath
        );

        return storedFile;
    }

    // =====================================================================
    // STUDENT DOCUMENTS
    // =====================================================================

    public StoredFile storeStudentDocument(
            MultipartFile file,
            ErpStudent student,
            String filePrefix
    ) {
        return storeMultipartFile(
                file,
                student,
                "documents",
                filePrefix,
                MAX_DOCUMENT_BYTES,
                DOCUMENT_MIME_TYPES
        );
    }

    public StoredFile replaceStudentDocument(
            MultipartFile file,
            ErpStudent student,
            String filePrefix,
            String existingRelativePath
    ) {
        StoredFile storedFile =
                storeStudentDocument(
                        file,
                        student,
                        filePrefix
                );

        scheduleDeleteAfterCommit(
                existingRelativePath
        );

        return storedFile;
    }

    // =====================================================================
    // ACADEMIC-HISTORY DOCUMENTS
    // =====================================================================

    public StoredFile storeAcademicHistoryDocument(
            MultipartFile file,
            ErpStudent student,
            String filePrefix
    ) {
        return storeMultipartFile(
                file,
                student,
                "academic-history",
                filePrefix,
                MAX_DOCUMENT_BYTES,
                DOCUMENT_MIME_TYPES
        );
    }

    public StoredFile replaceAcademicHistoryDocument(
            MultipartFile file,
            ErpStudent student,
            String filePrefix,
            String existingRelativePath
    ) {
        StoredFile storedFile =
                storeAcademicHistoryDocument(
                        file,
                        student,
                        filePrefix
                );

        scheduleDeleteAfterCommit(
                existingRelativePath
        );

        return storedFile;
    }

    // =====================================================================
    // PRIVATE FILE READING
    // =====================================================================

    /**
     * The caller must validate Student and branch ownership before calling
     * this method.
     */
    public Resource loadPrivateFile(
            String relativePath
    ) {
        Path filePath =
                resolveStoredFile(
                        relativePath
                );

        try {
            Resource resource =
                    new UrlResource(
                            filePath.toUri()
                    );

            if (!resource.exists() || !resource.isReadable()) {
                throw new ResourceNotFoundException(
                        "Student file was not found."
                );
            }

            return resource;
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Could not read the Student file.",
                    exception
            );
        }
    }

    public String detectContentType(
            String relativePath
    ) {
        Path filePath =
                resolveStoredFile(
                        relativePath
                );

        try {
            byte[] prefix =
                    readPrefix(
                            filePath
                    );

            String detectedType =
                    detectMimeType(
                            prefix
                    );

            if (detectedType != null) {
                return detectedType;
            }

            String probedType =
                    Files.probeContentType(
                            filePath
                    );

            return StringUtils.hasText(probedType)
                    ? normalizeMimeType(probedType)
                    : "application/octet-stream";
        } catch (IOException exception) {
            return "application/octet-stream";
        }
    }

    public long getStoredFileSize(
            String relativePath
    ) {
        try {
            return Files.size(
                    resolveStoredFile(relativePath)
            );
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Could not read Student file size.",
                    exception
            );
        }
    }

    public String getStoredFilename(
            String relativePath
    ) {
        return resolveStoredFile(relativePath)
                .getFileName()
                .toString();
    }

    // =====================================================================
    // FILE DELETION
    // =====================================================================

    public void deletePrivateFile(
            String relativePath
    ) {
        if (!StringUtils.hasText(relativePath)) {
            return;
        }

        Path filePath =
                resolvePathInsideRoot(
                        relativePath
                );

        try {
            if (
                    Files.exists(
                            filePath,
                            LinkOption.NOFOLLOW_LINKS
                    )
                            && Files.isSymbolicLink(filePath)
            ) {
                throw new SecurityException(
                        "Symbolic-link Student files are not allowed."
                );
            }

            Files.deleteIfExists(
                    filePath
            );
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Could not delete the private Student file.",
                    exception
            );
        }
    }

    public void deletePrivateFiles(
            Collection<String> relativePaths
    ) {
        if (relativePaths == null || relativePaths.isEmpty()) {
            return;
        }

        Set<String> uniquePaths =
                new LinkedHashSet<>(
                        relativePaths
                );

        for (String relativePath : uniquePaths) {
            deletePrivateFile(
                    relativePath
            );
        }
    }

    public void deletePrivateFilesQuietly(
            Collection<String> relativePaths
    ) {
        if (relativePaths == null || relativePaths.isEmpty()) {
            return;
        }

        for (
                String relativePath
                : new LinkedHashSet<>(relativePaths)
        ) {
            try {
                deletePrivateFile(
                        relativePath
                );
            } catch (RuntimeException ignored) {
                /*
                 * Cleanup must not hide the original application failure.
                 */
            }
        }
    }

    public void scheduleDeleteAfterCommit(
            String relativePath
    ) {
        if (!StringUtils.hasText(relativePath)) {
            return;
        }

        scheduleDeleteAfterCommit(
                List.of(relativePath)
        );
    }

    public void scheduleDeleteAfterCommit(
            Collection<String> relativePaths
    ) {
        List<String> paths =
                normalizeStoredPaths(
                        relativePaths
                );

        if (paths.isEmpty()) {
            return;
        }

        if (
                !TransactionSynchronizationManager
                        .isSynchronizationActive()
        ) {
            deletePrivateFiles(
                    paths
            );
            return;
        }

        TransactionSynchronizationManager
                .registerSynchronization(
                        new TransactionSynchronization() {

                            @Override
                            public void afterCommit() {
                                deletePrivateFilesQuietly(
                                        paths
                                );
                            }
                        }
                );
    }

    private void scheduleNewFileRollbackCleanup(
            String relativePath
    ) {
        if (
                !StringUtils.hasText(relativePath)
                        || !TransactionSynchronizationManager
                        .isSynchronizationActive()
        ) {
            return;
        }

        TransactionSynchronizationManager
                .registerSynchronization(
                        new TransactionSynchronization() {

                            @Override
                            public void afterCompletion(
                                    int status
                            ) {
                                if (
                                        status
                                                != TransactionSynchronization
                                                .STATUS_COMMITTED
                                ) {
                                    deletePrivateFilesQuietly(
                                            List.of(relativePath)
                                    );
                                }
                            }
                        }
                );
    }

    // =====================================================================
    // MULTIPART VALIDATION AND STORAGE
    // =====================================================================

    private StoredFile storeMultipartFile(
            MultipartFile multipartFile,
            ErpStudent student,
            String documentGroup,
            String filePrefix,
            long maximumBytes,
            Set<String> allowedMimeTypes
    ) {
        requireStudentStorageIdentity(
                student
        );

        ValidatedUpload upload =
                validateUpload(
                        multipartFile,
                        maximumBytes,
                        allowedMimeTypes
                );

        Path targetDirectory =
                buildStudentDirectory(
                        student,
                        documentGroup
                );

        String storedFileName =
                sanitizeFilePrefix(filePrefix)
                        + "-"
                        + UUID.randomUUID()
                        + "."
                        + upload.extension();

        Path targetFile =
                targetDirectory
                        .resolve(storedFileName)
                        .normalize();

        ensureInsideStorageRoot(
                targetFile
        );

        Path temporaryFile =
                null;

        try {
            Files.createDirectories(
                    targetDirectory
            );

            ensureNoSymbolicLink(
                    targetDirectory
            );

            temporaryFile =
                    Files.createTempFile(
                            targetDirectory,
                            ".student-upload-",
                            ".tmp"
                    );

            ensureInsideStorageRoot(
                    temporaryFile
            );

            Files.write(
                    temporaryFile,
                    upload.bytes(),
                    StandardOpenOption.WRITE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );

            moveAtomically(
                    temporaryFile,
                    targetFile
            );

            String relativePath =
                    toRelativePath(
                            targetFile
                    );

            scheduleNewFileRollbackCleanup(
                    relativePath
            );

            return new StoredFile(
                    relativePath,
                    storedFileName,
                    upload.originalFileName(),
                    upload.mimeType(),
                    upload.extension(),
                    upload.bytes().length
            );
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Could not store the private Student file.",
                    exception
            );
        } finally {
            if (temporaryFile != null) {
                try {
                    Files.deleteIfExists(
                            temporaryFile
                    );
                } catch (IOException ignored) {
                    /*
                     * Temporary-file cleanup failure must not hide the
                     * original storage result.
                     */
                }
            }
        }
    }

    private ValidatedUpload validateUpload(
            MultipartFile multipartFile,
            long maximumBytes,
            Set<String> allowedMimeTypes
    ) {
        if (
                multipartFile == null
                        || multipartFile.isEmpty()
        ) {
            throw new BadRequestException(
                    "Uploaded file is required."
            );
        }

        long declaredSize =
                multipartFile.getSize();

        if (declaredSize <= 0) {
            throw new BadRequestException(
                    "Uploaded file is empty."
            );
        }

        if (declaredSize > maximumBytes) {
            throw new BadRequestException(
                    "Uploaded file exceeds the maximum allowed size of "
                            + toMegabytes(maximumBytes)
                            + " MB."
            );
        }

        String originalFileName =
                sanitizeOriginalFilename(
                        multipartFile.getOriginalFilename()
                );

        String declaredMimeType =
                normalizeMimeType(
                        multipartFile.getContentType()
                );

        if (!allowedMimeTypes.contains(declaredMimeType)) {
            throw new BadRequestException(
                    "Unsupported Student file type."
            );
        }

        byte[] bytes;

        try {
            bytes =
                    multipartFile.getBytes();
        } catch (IOException exception) {
            throw new BadRequestException(
                    "Uploaded file could not be read."
            );
        }

        if (bytes.length == 0) {
            throw new BadRequestException(
                    "Uploaded file is empty."
            );
        }

        if (bytes.length > maximumBytes) {
            throw new BadRequestException(
                    "Uploaded file exceeds the maximum allowed size of "
                            + toMegabytes(maximumBytes)
                            + " MB."
            );
        }

        if (bytes.length != declaredSize) {
            throw new BadRequestException(
                    "Uploaded file size does not match its metadata."
            );
        }

        String detectedMimeType =
                detectMimeType(
                        bytes
                );

        if (detectedMimeType == null) {
            throw new BadRequestException(
                    "Uploaded file format could not be verified."
            );
        }

        if (!allowedMimeTypes.contains(detectedMimeType)) {
            throw new BadRequestException(
                    "Unsupported Student file format."
            );
        }

        if (!detectedMimeType.equals(declaredMimeType)) {
            throw new BadRequestException(
                    "Uploaded file content does not match its content type."
            );
        }

        validateOriginalFilenameExtension(
                originalFileName,
                detectedMimeType
        );

        return new ValidatedUpload(
                bytes,
                originalFileName,
                detectedMimeType,
                extensionForMimeType(detectedMimeType)
        );
    }

    // =====================================================================
    // FILE-SIGNATURE VALIDATION
    // =====================================================================

    private String detectMimeType(
            byte[] bytes
    ) {
        if (isJpeg(bytes)) {
            return "image/jpeg";
        }

        if (isPng(bytes)) {
            return "image/png";
        }

        if (isWebp(bytes)) {
            return "image/webp";
        }

        if (isPdf(bytes)) {
            return "application/pdf";
        }

        return null;
    }

    private boolean isJpeg(
            byte[] bytes
    ) {
        return bytes.length >= 3
                && unsigned(bytes[0]) == 0xFF
                && unsigned(bytes[1]) == 0xD8
                && unsigned(bytes[2]) == 0xFF;
    }

    private boolean isPng(
            byte[] bytes
    ) {
        int[] signature =
                {
                        0x89,
                        0x50,
                        0x4E,
                        0x47,
                        0x0D,
                        0x0A,
                        0x1A,
                        0x0A
                };

        return matchesSignature(
                bytes,
                signature
        );
    }

    private boolean isWebp(
            byte[] bytes
    ) {
        return bytes.length >= 12
                && matchesAscii(
                bytes,
                0,
                "RIFF"
        )
                && matchesAscii(
                bytes,
                8,
                "WEBP"
        );
    }

    private boolean isPdf(
            byte[] bytes
    ) {
        return matchesAscii(
                bytes,
                0,
                "%PDF-"
        );
    }

    private boolean matchesSignature(
            byte[] bytes,
            int[] signature
    ) {
        if (bytes.length < signature.length) {
            return false;
        }

        for (
                int index = 0;
                index < signature.length;
                index++
        ) {
            if (unsigned(bytes[index]) != signature[index]) {
                return false;
            }
        }

        return true;
    }

    private boolean matchesAscii(
            byte[] bytes,
            int offset,
            String expected
    ) {
        if (bytes.length < offset + expected.length()) {
            return false;
        }

        for (
                int index = 0;
                index < expected.length();
                index++
        ) {
            if (
                    bytes[offset + index]
                            != (byte) expected.charAt(index)
            ) {
                return false;
            }
        }

        return true;
    }

    private int unsigned(
            byte value
    ) {
        return value & 0xFF;
    }

    private String extensionForMimeType(
            String mimeType
    ) {
        return switch (mimeType) {
            case "image/jpeg" -> "jpg";
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            case "application/pdf" -> "pdf";
            default -> throw new BadRequestException(
                    "Unsupported Student file type."
            );
        };
    }

    private void validateOriginalFilenameExtension(
            String originalFileName,
            String detectedMimeType
    ) {
        String extension =
                extractOriginalExtension(
                        originalFileName
                );

        if (!StringUtils.hasText(extension)) {
            throw new BadRequestException(
                    "Uploaded file must have a valid extension."
            );
        }

        Set<String> acceptedExtensions =
                switch (detectedMimeType) {
                    case "image/jpeg" ->
                            Set.of(
                                    "jpg",
                                    "jpeg"
                            );

                    case "image/png" ->
                            Set.of("png");

                    case "image/webp" ->
                            Set.of("webp");

                    case "application/pdf" ->
                            Set.of("pdf");

                    default ->
                            Set.of();
                };

        if (!acceptedExtensions.contains(extension)) {
            throw new BadRequestException(
                    "Uploaded file name does not match the actual file type."
            );
        }
    }

    // =====================================================================
    // STORAGE PATHS
    // =====================================================================

    private Path buildStudentDirectory(
            ErpStudent student,
            String documentGroup
    ) {
        requireStudentStorageIdentity(
                student
        );

        Branch branch =
                student.getBranch();

        String branchFolder =
                sanitizePathSegment(
                        buildBranchFolder(branch)
                );

        String studentFolder =
                sanitizePathSegment(
                        student.getStudentCode()
                                + "-"
                                + student.getFullName()
                );

        String safeDocumentGroup =
                sanitizePathSegment(
                        documentGroup
                )
                        .toLowerCase(
                                Locale.ROOT
                        );

        Path directory =
                privateStorageRoot
                        .resolve(BRANCH_DETAILS_DIRECTORY)
                        .resolve(branchFolder)
                        .resolve(STUDENTS_DIRECTORY)
                        .resolve(studentFolder)
                        .resolve(safeDocumentGroup)
                        .normalize();

        ensureInsideStorageRoot(
                directory
        );

        return directory;
    }

    private String buildBranchFolder(
            Branch branch
    ) {
        String schoolCode =
                StringUtils.hasText(branch.getSchoolCode())
                        ? branch.getSchoolCode()
                        : "branch-" + branch.getBranchId();

        String branchName =
                StringUtils.hasText(branch.getBranchName())
                        ? branch.getBranchName()
                        : "school";

        String location =
                StringUtils.hasText(branch.getBranchLocation())
                        ? branch.getBranchLocation()
                        : null;

        return location == null
                ? schoolCode + "-" + branchName
                : schoolCode
                  + "-"
                  + branchName
                  + ","
                  + location;
    }

    private void requireStudentStorageIdentity(
            ErpStudent student
    ) {
        Objects.requireNonNull(
                student,
                "Student entity is required."
        );

        if (student.getBranch() == null) {
            throw new IllegalStateException(
                    "Student branch is required before storing files."
            );
        }

        if (!StringUtils.hasText(student.getStudentCode())) {
            throw new IllegalStateException(
                    "Student code is required before storing files."
            );
        }

        if (!StringUtils.hasText(student.getFullName())) {
            throw new IllegalStateException(
                    "Student full name is required before storing files."
            );
        }
    }

    private Path resolveStoredFile(
            String relativePath
    ) {
        Path filePath =
                resolvePathInsideRoot(
                        relativePath
                );

        if (
                !Files.exists(
                        filePath,
                        LinkOption.NOFOLLOW_LINKS
                )
                        || !Files.isRegularFile(
                        filePath,
                        LinkOption.NOFOLLOW_LINKS
                )
                        || Files.isSymbolicLink(filePath)
        ) {
            throw new ResourceNotFoundException(
                    "Student file was not found."
            );
        }

        return filePath;
    }

    private Path resolvePathInsideRoot(
            String relativePath
    ) {
        if (!StringUtils.hasText(relativePath)) {
            throw new ResourceNotFoundException(
                    "Student file was not found."
            );
        }

        Path suppliedPath =
                Path.of(
                        relativePath.trim()
                );

        if (suppliedPath.isAbsolute()) {
            throw new SecurityException(
                    "Absolute Student file paths are not allowed."
            );
        }

        Path resolvedPath =
                privateStorageRoot
                        .resolve(suppliedPath)
                        .normalize();

        ensureInsideStorageRoot(
                resolvedPath
        );

        return resolvedPath;
    }

    private void ensureInsideStorageRoot(
            Path path
    ) {
        Path normalizedPath =
                path.toAbsolutePath()
                        .normalize();

        if (!normalizedPath.startsWith(privateStorageRoot)) {
            throw new SecurityException(
                    "Invalid Student storage path."
            );
        }
    }

    private void ensureNoSymbolicLink(
            Path path
    ) throws IOException {
        Path normalizedPath =
                path.toAbsolutePath()
                        .normalize();

        ensureInsideStorageRoot(
                normalizedPath
        );

        Path current =
                privateStorageRoot;

        Path relative =
                privateStorageRoot.relativize(
                        normalizedPath
                );

        for (Path segment : relative) {
            current =
                    current.resolve(segment);

            if (
                    Files.exists(
                            current,
                            LinkOption.NOFOLLOW_LINKS
                    )
                            && Files.isSymbolicLink(current)
            ) {
                throw new SecurityException(
                        "Symbolic links are not allowed in Student storage."
                );
            }
        }
    }

    private String toRelativePath(
            Path targetFile
    ) {
        ensureInsideStorageRoot(
                targetFile
        );

        return privateStorageRoot
                .relativize(
                        targetFile
                                .toAbsolutePath()
                                .normalize()
                )
                .toString()
                .replace('\\', '/');
    }

    // =====================================================================
    // NORMALIZATION
    // =====================================================================

    private String normalizeMimeType(
            String contentType
    ) {
        if (!StringUtils.hasText(contentType)) {
            throw new BadRequestException(
                    "Uploaded file content type is required."
            );
        }

        String normalized =
                contentType.trim()
                        .toLowerCase(
                                Locale.ROOT
                        );

        int parameterIndex =
                normalized.indexOf(';');

        if (parameterIndex >= 0) {
            normalized =
                    normalized.substring(
                                    0,
                                    parameterIndex
                            )
                            .trim();
        }

        return switch (normalized) {
            case "image/jpg",
                 "image/pjpeg" ->
                    "image/jpeg";

            case "application/x-pdf" ->
                    "application/pdf";

            default ->
                    normalized;
        };
    }

    private String sanitizeOriginalFilename(
            String originalFileName
    ) {
        if (!StringUtils.hasText(originalFileName)) {
            throw new BadRequestException(
                    "Uploaded file name is required."
            );
        }

        String sanitized =
                originalFileName
                        .trim()
                        .replace('\\', '/');

        int slashIndex =
                sanitized.lastIndexOf('/');

        if (slashIndex >= 0) {
            sanitized =
                    sanitized.substring(
                            slashIndex + 1
                    );
        }

        sanitized =
                sanitized.replaceAll(
                        "\\p{Cntrl}",
                        ""
                );

        if (
                !StringUtils.hasText(sanitized)
                        || ".".equals(sanitized)
                        || "..".equals(sanitized)
        ) {
            throw new BadRequestException(
                    "Uploaded file name is invalid."
            );
        }

        if (sanitized.length() > 255) {
            sanitized =
                    sanitized.substring(
                            sanitized.length() - 255
                    );
        }

        return sanitized;
    }

    private String extractOriginalExtension(
            String originalFileName
    ) {
        int dotIndex =
                originalFileName.lastIndexOf('.');

        if (
                dotIndex < 0
                        || dotIndex
                        == originalFileName.length() - 1
        ) {
            return null;
        }

        return originalFileName
                .substring(dotIndex + 1)
                .toLowerCase(Locale.ROOT);
    }

    private String sanitizePathSegment(
            String value
    ) {
        if (!StringUtils.hasText(value)) {
            return "unknown";
        }

        String sanitized =
                value.trim()
                        .replaceAll(
                                "[\\\\/:*?\"<>|\\p{Cntrl}]",
                                "-"
                        )
                        .replaceAll(
                                "\\s+",
                                " "
                        )
                        .replaceAll(
                                "\\.{2,}",
                                "."
                        );

        sanitized =
                sanitized.replaceAll(
                        "^[. ]+|[. ]+$",
                        ""
                );

        if (!StringUtils.hasText(sanitized)) {
            return "unknown";
        }

        return sanitized.length() > 120
                ? sanitized.substring(0, 120)
                : sanitized;
    }

    private String sanitizeFilePrefix(
            String filePrefix
    ) {
        String sanitized =
                sanitizePathSegment(
                        filePrefix
                )
                        .toLowerCase(
                                Locale.ROOT
                        )
                        .replace(' ', '-');

        return StringUtils.hasText(sanitized)
                ? sanitized
                : "student-file";
    }

    private List<String> normalizeStoredPaths(
            Collection<String> relativePaths
    ) {
        if (relativePaths == null || relativePaths.isEmpty()) {
            return List.of();
        }

        return relativePaths.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .distinct()
                .toList();
    }

    private long toMegabytes(
            long bytes
    ) {
        return bytes / ONE_MEGABYTE;
    }

    private void moveAtomically(
            Path source,
            Path target
    ) throws IOException {
        try {
            Files.move(
                    source,
                    target,
                    StandardCopyOption.ATOMIC_MOVE
            );
        } catch (AtomicMoveNotSupportedException exception) {
            Files.move(
                    source,
                    target,
                    StandardCopyOption.REPLACE_EXISTING
            );
        }
    }

    private byte[] readPrefix(
            Path filePath
    ) throws IOException {
        int maximumPrefixLength =
                16;

        try (
                InputStream inputStream =
                        Files.newInputStream(filePath)
        ) {
            return inputStream.readNBytes(
                    maximumPrefixLength
            );
        }
    }

    // =====================================================================
    // RESULT RECORDS
    // =====================================================================

    public record StoredFile(
            String relativePath,
            String storedFileName,
            String originalFileName,
            String mimeType,
            String extension,
            long size
    ) {
    }

    private record ValidatedUpload(
            byte[] bytes,
            String originalFileName,
            String mimeType,
            String extension
    ) {
    }
}