package com.erp.montfortuganda.employee.service;

import com.erp.montfortuganda.employee.dto.request.EmployeeDocumentRequest;
import com.erp.montfortuganda.employee.dto.request.EmployeeExperienceRequest;
import com.erp.montfortuganda.employee.dto.request.EmployeeQualificationRequest;
import com.erp.montfortuganda.employee.dto.request.EmployeeRegistrationRequest;
import com.erp.montfortuganda.employee.dto.request.EmployeeUpdateRequest;
import com.erp.montfortuganda.employee.entity.ErpEmployee;
import com.erp.montfortuganda.employee.entity.ErpEmployeeDocument;
import com.erp.montfortuganda.employee.entity.ErpEmployeeExperience;
import com.erp.montfortuganda.employee.entity.ErpEmployeeQualification;
import com.erp.montfortuganda.exception.BadRequestException;
import com.erp.montfortuganda.exception.ResourceNotFoundException;
import com.erp.montfortuganda.school.entity.Branch;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Base64;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Secure private-file storage for the Employee module.
  * <p>
 * Responsibilities:
  * <p>
 * - decodes Base64/data-URL uploads received from the Employee frontend;
 * - validates declared size, MIME type and actual file signature;
 * - stores files only under the configured private ERP storage root;
 * - generates safe, non-user-controlled stored filenames;
 * - never exposes or accepts permanent storage paths from the browser;
 * - schedules newly stored files for cleanup when a transaction rolls back;
 * - schedules replaced files for deletion only after a successful commit;
 * - loads private files only from normalized, non-symbolic paths.
  * <p>
 * Supported file types:
  * <p>
 * - profile photo and signature: JPEG, PNG and WEBP;
 * - qualifications, experience files and Employee documents:
 *   PDF, JPEG, PNG and WEBP.
 */
@SuppressWarnings("unused")
@Service
public class EmployeeFileService {

    private static final long ONE_MEGABYTE =
            1024L * 1024L;

    private static final long MAX_PROFILE_PHOTO_BYTES =
            2L * ONE_MEGABYTE;

    private static final long MAX_SIGNATURE_BYTES =
            ONE_MEGABYTE;

    private static final long MAX_PRIVATE_DOCUMENT_BYTES =
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

    //noinspection SpellCheckingInspection
    private static final String BRANCH_DETAILS_DIRECTORY =
            "branchdetails";

    private static final String STAFF_DIRECTORY =
            "staff";

    private final Path privateStorageRoot;

    public EmployeeFileService(
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

            if (
                    Files.isSymbolicLink(
                            privateStorageRoot
                    )
            ) {
                throw new IllegalStateException(
                        "Private ERP storage root cannot be a symbolic link."
                );
            }
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Could not initialize private Employee storage.",
                    exception
            );
        }
    }

    // =====================================================================
    // EMPLOYEE MASTER FILES
    // =====================================================================

    /**
     * Stores the mandatory profile photo and optional signature for a newly
     * registered Employee.
     */
    public void storeNewEmployeeFiles(
            EmployeeRegistrationRequest request,
            ErpEmployee employee
    ) {
        Objects.requireNonNull(
                request,
                "Employee registration request is required."
        );

        requireEmployeeStorageIdentity(employee);

        if (hasText(request.profilePhotoData())) {
            StoredFile profilePhoto =
                    storeEncodedFile(
                            request.profilePhotoData(),
                            request.profilePhotoFileName(),
                            request.profilePhotoContentType(),
                            request.profilePhotoFileSize(),
                            MAX_PROFILE_PHOTO_BYTES,
                            IMAGE_MIME_TYPES,
                            employee,
                            "profile",
                            "profile-photo"
                    );

            employee.setProfilePhoto(
                    profilePhoto.relativePath()
            );
        } else {
            employee.setProfilePhoto(null);
        }

        if (hasText(request.signatureData())) {
            StoredFile signature =
                    storeEncodedFile(
                            request.signatureData(),
                            request.signatureFileName(),
                            request.signatureContentType(),
                            request.signatureFileSize(),
                            MAX_SIGNATURE_BYTES,
                            IMAGE_MIME_TYPES,
                            employee,
                            "signature",
                            "signature"
                    );

            employee.setSignatureFile(
                    signature.relativePath()
            );
        } else {
            employee.setSignatureFile(null);
        }
    }

    /**
     * Stores replacement profile/signature files supplied during Employee
     * update. Existing paths are preserved when no replacement is supplied.
      * <p>
     * Old files are scheduled for deletion only after the surrounding
     * database transaction commits successfully.
     */
    public void replaceEmployeeMasterFiles(
            EmployeeUpdateRequest request,
            ErpEmployee employee
    ) {
        Objects.requireNonNull(
                request,
                "Employee update request is required."
        );
        requireEmployeeStorageIdentity(employee);

        if (hasText(request.profilePhotoData())) {
            String oldProfilePhoto =
                    employee.getProfilePhoto();

            StoredFile newProfilePhoto =
                    storeEncodedFile(
                            request.profilePhotoData(),
                            request.profilePhotoFileName(),
                            request.profilePhotoContentType(),
                            request.profilePhotoFileSize(),
                            MAX_PROFILE_PHOTO_BYTES,
                            IMAGE_MIME_TYPES,
                            employee,
                            "profile",
                            "profile-photo"
                    );

            employee.setProfilePhoto(
                    newProfilePhoto.relativePath()
            );

            scheduleDeleteAfterCommit(
                    oldProfilePhoto
            );
        }

        if (hasText(request.signatureFileData())) {
            String oldSignature =
                    employee.getSignatureFile();

            StoredFile newSignature =
                    storeEncodedFile(
                            request.signatureFileData(),
                            request.signatureFileName(),
                            request.signatureContentType(),
                            request.signatureFileSize(),
                            MAX_SIGNATURE_BYTES,
                            IMAGE_MIME_TYPES,
                            employee,
                            "signature",
                            "signature"
                    );

            employee.setSignatureFile(
                    newSignature.relativePath()
            );

            scheduleDeleteAfterCommit(
                    oldSignature
            );
        }
    }

    // =====================================================================
    // QUALIFICATION FILES
    // =====================================================================

    public void storeNewQualificationFile(
            EmployeeQualificationRequest request,
            ErpEmployeeQualification qualification
    ) {
        Objects.requireNonNull(
                request,
                "Employee qualification request is required."
        );
        Objects.requireNonNull(
                qualification,
                "Employee qualification entity is required."
        );

        if (!hasText(request.fileData())) {
            return;
        }

        ErpEmployee employee =
                requireNestedEmployee(
                        qualification.getEmployee()
                );

        StoredFile storedFile =
                storeEncodedFile(
                        request.fileData(),
                        request.fileName(),
                        request.contentType(),
                        request.fileSize(),
                        MAX_PRIVATE_DOCUMENT_BYTES,
                        DOCUMENT_MIME_TYPES,
                        employee,
                        "qualifications",
                        buildQualificationPrefix(
                                qualification
                        )
                );

        qualification.setEmployeeQualificationDocumentFile(
                storedFile.relativePath()
        );
    }

    public void replaceQualificationFile(
            EmployeeQualificationRequest request,
            ErpEmployeeQualification qualification
    ) {
        Objects.requireNonNull(
                request,
                "Employee qualification request is required."
        );
        Objects.requireNonNull(
                qualification,
                "Employee qualification entity is required."
        );

        if (!hasText(request.fileData())) {
            return;
        }

        ErpEmployee employee =
                requireNestedEmployee(
                        qualification.getEmployee()
                );

        String oldPath =
                qualification
                        .getEmployeeQualificationDocumentFile();

        StoredFile storedFile =
                storeEncodedFile(
                        request.fileData(),
                        request.fileName(),
                        request.contentType(),
                        request.fileSize(),
                        MAX_PRIVATE_DOCUMENT_BYTES,
                        DOCUMENT_MIME_TYPES,
                        employee,
                        "qualifications",
                        buildQualificationPrefix(
                                qualification
                        )
                );

        qualification.setEmployeeQualificationDocumentFile(
                storedFile.relativePath()
        );

        scheduleDeleteAfterCommit(
                oldPath
        );
    }

    // =====================================================================
    // EXPERIENCE FILES
    // =====================================================================

    public void storeNewExperienceFiles(
            EmployeeExperienceRequest request,
            ErpEmployeeExperience experience
    ) {
        Objects.requireNonNull(
                request,
                "Employee experience request is required."
        );
        Objects.requireNonNull(
                experience,
                "Employee experience entity is required."
        );

        ErpEmployee employee =
                requireNestedEmployee(
                        experience.getEmployee()
                );

        if (
                hasText(
                        request.experienceCertificateFileData()
                )
        ) {
            StoredFile certificate =
                    storeEncodedFile(
                            request.experienceCertificateFileData(),
                            request.experienceCertificateFileName(),
                            request.experienceCertificateContentType(),
                            request.experienceCertificateFileSize(),
                            MAX_PRIVATE_DOCUMENT_BYTES,
                            DOCUMENT_MIME_TYPES,
                            employee,
                            "experience",
                            buildExperiencePrefix(
                                    "experience-certificate",
                                    experience
                            )
                    );

            experience.setEmployeeExperienceExperienceCertificateFile(
                    certificate.relativePath()
            );
        }

        if (
                hasText(
                        request.relievingLetterFileData()
                )
        ) {
            StoredFile relievingLetter =
                    storeEncodedFile(
                            request.relievingLetterFileData(),
                            request.relievingLetterFileName(),
                            request.relievingLetterContentType(),
                            request.relievingLetterFileSize(),
                            MAX_PRIVATE_DOCUMENT_BYTES,
                            DOCUMENT_MIME_TYPES,
                            employee,
                            "experience",
                            buildExperiencePrefix(
                                    "relieving-letter",
                                    experience
                            )
                    );

            experience.setEmployeeExperienceRelievingLetterFile(
                    relievingLetter.relativePath()
            );
        }
    }

    public void replaceExperienceFiles(
            EmployeeExperienceRequest request,
            ErpEmployeeExperience experience
    ) {
        Objects.requireNonNull(
                request,
                "Employee experience request is required."
        );
        Objects.requireNonNull(
                experience,
                "Employee experience entity is required."
        );

        ErpEmployee employee =
                requireNestedEmployee(
                        experience.getEmployee()
                );

        if (
                hasText(
                        request.experienceCertificateFileData()
                )
        ) {
            String oldCertificate =
                    experience
                            .getEmployeeExperienceExperienceCertificateFile();

            StoredFile newCertificate =
                    storeEncodedFile(
                            request.experienceCertificateFileData(),
                            request.experienceCertificateFileName(),
                            request.experienceCertificateContentType(),
                            request.experienceCertificateFileSize(),
                            MAX_PRIVATE_DOCUMENT_BYTES,
                            DOCUMENT_MIME_TYPES,
                            employee,
                            "experience",
                            buildExperiencePrefix(
                                    "experience-certificate",
                                    experience
                            )
                    );

            experience.setEmployeeExperienceExperienceCertificateFile(
                    newCertificate.relativePath()
            );

            scheduleDeleteAfterCommit(
                    oldCertificate
            );
        }

        if (
                hasText(
                        request.relievingLetterFileData()
                )
        ) {
            String oldRelievingLetter =
                    experience
                            .getEmployeeExperienceRelievingLetterFile();

            StoredFile newRelievingLetter =
                    storeEncodedFile(
                            request.relievingLetterFileData(),
                            request.relievingLetterFileName(),
                            request.relievingLetterContentType(),
                            request.relievingLetterFileSize(),
                            MAX_PRIVATE_DOCUMENT_BYTES,
                            DOCUMENT_MIME_TYPES,
                            employee,
                            "experience",
                            buildExperiencePrefix(
                                    "relieving-letter",
                                    experience
                            )
                    );

            experience.setEmployeeExperienceRelievingLetterFile(
                    newRelievingLetter.relativePath()
            );

            scheduleDeleteAfterCommit(
                    oldRelievingLetter
            );
        }
    }

    // =====================================================================
    // GENERAL EMPLOYEE DOCUMENT FILES
    // =====================================================================

    public void storeNewDocumentFile(
            EmployeeDocumentRequest request,
            ErpEmployeeDocument document
    ) {
        Objects.requireNonNull(
                request,
                "Employee document request is required."
        );
        Objects.requireNonNull(
                document,
                "Employee document entity is required."
        );

        ErpEmployee employee =
                requireNestedEmployee(
                        document.getEmployee()
                );

        StoredFile storedFile =
                storeEncodedFile(
                        request.fileData(),
                        request.fileName(),
                        request.contentType(),
                        request.fileSize(),
                        MAX_PRIVATE_DOCUMENT_BYTES,
                        DOCUMENT_MIME_TYPES,
                        employee,
                        "documents",
                        buildDocumentPrefix(document)
                );

        applyDocumentFileMetadata(
                request,
                document,
                storedFile
        );
    }

    public void replaceDocumentFile(
            EmployeeDocumentRequest request,
            ErpEmployeeDocument document
    ) {
        Objects.requireNonNull(
                request,
                "Employee document request is required."
        );
        Objects.requireNonNull(
                document,
                "Employee document entity is required."
        );

        if (!hasText(request.fileData())) {
            return;
        }

        ErpEmployee employee =
                requireNestedEmployee(
                        document.getEmployee()
                );

        String oldPath =
                document.getEmployeeDocumentFilePath();

        StoredFile storedFile =
                storeEncodedFile(
                        request.fileData(),
                        request.fileName(),
                        request.contentType(),
                        request.fileSize(),
                        MAX_PRIVATE_DOCUMENT_BYTES,
                        DOCUMENT_MIME_TYPES,
                        employee,
                        "documents",
                        buildDocumentPrefix(document)
                );

        applyDocumentFileMetadata(
                request,
                document,
                storedFile
        );

        scheduleDeleteAfterCommit(
                oldPath
        );
    }

    private void applyDocumentFileMetadata(
            EmployeeDocumentRequest request,
            ErpEmployeeDocument document,
            StoredFile storedFile
    ) {
        document.setEmployeeDocumentFileName(
                storedFile.storedFileName()
        );
        document.setEmployeeDocumentOriginalFileName(
                sanitizeOriginalFilename(
                        request.fileName()
                )
        );
        document.setEmployeeDocumentFilePath(
                storedFile.relativePath()
        );
        document.setEmployeeDocumentFileExtension(
                storedFile.extension()
        );
        document.setEmployeeDocumentMimeType(
                storedFile.mimeType()
        );
        document.setEmployeeDocumentFileSize(
                storedFile.size()
        );
    }

    // =====================================================================
    // PRIVATE FILE READING
    // =====================================================================

    /**
     * Loads a previously authorized Employee private file.
      * <p>
     * Branch and Employee ownership must be verified by the caller before
     * passing an entity-stored path to this method.
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

            if (
                    !resource.exists()
                            || !resource.isReadable()
            ) {
                throw new ResourceNotFoundException(
                        "Employee file was not found."
                );
            }

            return resource;
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Could not read the Employee file.",
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

            String detectedMimeType =
                    detectMimeType(prefix);

            if (detectedMimeType != null) {
                return detectedMimeType;
            }

            String probedMimeType =
                    Files.probeContentType(
                            filePath
                    );

            return hasText(probedMimeType)
                    ? normalizeMimeType(
                    probedMimeType
            )
                    : "application/octet-stream";
        } catch (IOException exception) {
            return "application/octet-stream";
        }
    }

    public long getStoredFileSize(
            String relativePath
    ) {
        Path filePath =
                resolveStoredFile(
                        relativePath
                );

        try {
            return Files.size(filePath);
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Could not read Employee file size.",
                    exception
            );
        }
    }

    public String getStoredFilename(
            String relativePath
    ) {
        return resolveStoredFile(
                relativePath
        )
                .getFileName()
                .toString();
    }

    // =====================================================================
    // DELETION AND TRANSACTION CONSISTENCY
    // =====================================================================

    /**
     * Deletes one private file immediately.
      * <p>
     * Use scheduleDeleteAfterCommit(...) for files referenced by a database
     * record being updated in the current transaction.
     */
    public void deletePrivateFile(
            String relativePath
    ) {
        if (!hasText(relativePath)) {
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
                            && Files.isSymbolicLink(
                            filePath
                    )
            ) {
                throw new SecurityException(
                        "Symbolic-link Employee files are not allowed."
                );
            }

            Files.deleteIfExists(
                    filePath
            );
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Could not delete the private Employee file.",
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

        for (String relativePath : new LinkedHashSet<>(relativePaths)) {
            try {
                deletePrivateFile(
                        relativePath
                );
            } catch (RuntimeException ignored) {
                /*
                 * Cleanup must not hide the original application exception.
                 * Operational logging can be added through the project's
                 * preferred logger during final production cleanup.
                 */
            }
        }
    }

    public void scheduleDeleteAfterCommit(
            String relativePath
    ) {
        if (!hasText(relativePath)) {
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

    /**
     * Newly written files are removed automatically when the current database
     * transaction rolls back.
     */
    private void scheduleNewFileRollbackCleanup(
            String relativePath
    ) {
        if (
                !hasText(relativePath)
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
    // BASE64 DECODING, MIME VALIDATION AND STORAGE
    // =====================================================================

    private StoredFile storeEncodedFile(
            String fileData,
            String originalFilename,
            String declaredContentType,
            Long declaredFileSize,
            long maximumBytes,
            Set<String> allowedMimeTypes,
            ErpEmployee employee,
            String documentGroup,
            String filePrefix
    ) {
        requireUploadMetadata(
                fileData,
                originalFilename,
                declaredContentType,
                declaredFileSize
        );

        DecodedUpload upload =
                decodeAndValidateUpload(
                        fileData,
                        originalFilename,
                        declaredContentType,
                        declaredFileSize,
                        maximumBytes,
                        allowedMimeTypes
                );

        Path targetDirectory =
                buildEmployeeDirectory(
                        employee,
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

        try {
            Files.createDirectories(
                    targetDirectory
            );

            ensureNoSymbolicLink(
                    targetDirectory
            );

            Path temporaryFile =
                    Files.createTempFile(
                            targetDirectory,
                            ".employee-upload-",
                            ".tmp"
                    );

            ensureInsideStorageRoot(
                    temporaryFile
            );

            try {
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
            } finally {
                Files.deleteIfExists(
                        temporaryFile
                );
            }

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
                    sanitizeOriginalFilename(
                            originalFilename
                    ),
                    upload.mimeType(),
                    upload.extension(),
                    upload.bytes().length
            );
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Could not store the private Employee file.",
                    exception
            );
        }
    }

    private DecodedUpload decodeAndValidateUpload(
            String fileData,
            String originalFilename,
            String declaredContentType,
            Long declaredFileSize,
            long maximumBytes,
            Set<String> allowedMimeTypes
    ) {
        if (
                declaredFileSize == null
                        || declaredFileSize <= 0
        ) {
            throw new BadRequestException(
                    "Uploaded file size must be greater than zero."
            );
        }

        if (declaredFileSize > maximumBytes) {
            throw new BadRequestException(
                    "Uploaded file exceeds the maximum allowed size of "
                            + toMegabytes(maximumBytes)
                            + " MB."
            );
        }

        ParsedBase64 parsedBase64 =
                parseBase64Data(
                        fileData
                );

        String normalizedDeclaredType =
                normalizeMimeType(
                        declaredContentType
                );

        if (
                hasText(parsedBase64.mimeType())
                        && !normalizedDeclaredType.equals(
                        parsedBase64.mimeType()
                )
        ) {
            throw new BadRequestException(
                    "Uploaded file content type does not match its data."
            );
        }

        if (
                !allowedMimeTypes.contains(
                        normalizedDeclaredType
                )
        ) {
            throw new BadRequestException(
                    "Unsupported Employee file type."
            );
        }

        validateEncodedLength(
                parsedBase64.payload(),
                maximumBytes
        );

        byte[] bytes;

        try {
            bytes =
                    Base64.getDecoder()
                            .decode(
                                    parsedBase64.payload()
                            );
        } catch (IllegalArgumentException exception) {
            throw new BadRequestException(
                    "Uploaded file data is not valid Base64."
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

        if (bytes.length != declaredFileSize) {
            throw new BadRequestException(
                    "Uploaded file size does not match the submitted metadata."
            );
        }

        String detectedMimeType =
                detectMimeType(bytes);

        if (detectedMimeType == null) {
            throw new BadRequestException(
                    "Uploaded file format could not be verified."
            );
        }

        if (
                !normalizedDeclaredType.equals(
                        detectedMimeType
                )
        ) {
            throw new BadRequestException(
                    "Uploaded file extension or content type is invalid."
            );
        }

        if (
                !allowedMimeTypes.contains(
                        detectedMimeType
                )
        ) {
            throw new BadRequestException(
                    "Unsupported Employee file format."
            );
        }

        String extension =
                extensionForMimeType(
                        detectedMimeType
                );

        validateOriginalFilenameExtension(
                originalFilename,
                detectedMimeType
        );

        return new DecodedUpload(
                bytes,
                detectedMimeType,
                extension
        );
    }

    private ParsedBase64 parseBase64Data(
            String fileData
    ) {
        if (!hasText(fileData)) {
            throw new BadRequestException(
                    "Uploaded file data is required."
            );
        }

        String trimmed =
                fileData.trim();

        if (!trimmed.startsWith("data:")) {
            return new ParsedBase64(
                    null,
                    removeBase64Whitespace(trimmed)
            );
        }

        int commaIndex =
                trimmed.indexOf(',');

        if (commaIndex <= 5) {
            throw new BadRequestException(
                    "Uploaded file data URL is invalid."
            );
        }

        String metadata =
                trimmed.substring(
                        5,
                        commaIndex
                );

        String payload =
                trimmed.substring(
                        commaIndex + 1
                );

        String[] metadataParts =
                metadata.split(";");

        if (
                metadataParts.length < 2
                        || !"base64".equalsIgnoreCase(
                        metadataParts[
                                metadataParts.length - 1
                        ]
                )
        ) {
            throw new BadRequestException(
                    "Uploaded file data URL must use Base64 encoding."
            );
        }

        String mimeType =
                normalizeMimeType(
                        metadataParts[0]
                );

        return new ParsedBase64(
                mimeType,
                removeBase64Whitespace(payload)
        );
    }

    private String removeBase64Whitespace(
            String payload
    ) {
        if (!hasText(payload)) {
            throw new BadRequestException(
                    "Uploaded file data is empty."
            );
        }

        return payload.replaceAll(
                "\\s+",
                ""
        );
    }

    private void validateEncodedLength(
            String payload,
            long maximumBytes
    ) {
        long maximumEncodedCharacters =
                (
                        (maximumBytes + 2L) / 3L
                ) * 4L
                        + 16L;

        if (payload.length() > maximumEncodedCharacters) {
            throw new BadRequestException(
                    "Uploaded file data exceeds the maximum allowed size."
            );
        }
    }

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

        for (int index = 0; index < signature.length; index++) {
            if (
                    unsigned(
                            bytes[index]
                    )
                            != signature[index]
            ) {
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
        if (
                bytes.length
                        < offset + expected.length()
        ) {
            return false;
        }

        for (int index = 0; index < expected.length(); index++) {
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
                    "Unsupported Employee file type."
            );
        };
    }

    private void validateOriginalFilenameExtension(
            String originalFilename,
            String detectedMimeType
    ) {
        String extension =
                extractOriginalExtension(
                        originalFilename
                );

        if (!hasText(extension)) {
            return;
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

        if (
                !acceptedExtensions.contains(
                        extension
                )
        ) {
            throw new BadRequestException(
                    "Uploaded file name does not match the actual file type."
            );
        }
    }

    private String extractOriginalExtension(
            String originalFilename
    ) {
        String sanitized =
                sanitizeOriginalFilename(
                        originalFilename
                );

        int dotIndex =
                sanitized.lastIndexOf('.');

        if (
                dotIndex < 0
                        || dotIndex
                        == sanitized.length() - 1
        ) {
            return null;
        }

        return sanitized.substring(
                        dotIndex + 1
                )
                .toLowerCase(
                        Locale.ROOT
                );
    }

    private String normalizeMimeType(
            String contentType
    ) {
        if (!hasText(contentType)) {
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

    private void requireUploadMetadata(
            String fileData,
            String originalFilename,
            String contentType,
            Long fileSize
    ) {
        if (
                !hasText(fileData)
                        || !hasText(originalFilename)
                        || !hasText(contentType)
                        || fileSize == null
        ) {
            throw new BadRequestException(
                    "File data, file name, content type and file size are required."
            );
        }
    }

    // =====================================================================
    // STORAGE PATHS
    // =====================================================================

    private Path buildEmployeeDirectory(
            ErpEmployee employee,
            String documentGroup
    ) {
        requireEmployeeStorageIdentity(
                employee
        );

        Branch branch =
                employee.getBranch();

        String branchFolder =
                sanitizePathSegment(
                        buildBranchFolder(branch)
                );

        String employeeFolder =
                sanitizePathSegment(
                        employee.getEmployeeNo()
                                + "-"
                                + employee.getFullName()
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
                        .resolve(STAFF_DIRECTORY)
                        .resolve(employeeFolder)
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
                hasText(branch.getSchoolCode())
                        ? branch.getSchoolCode()
                        : "branch-" + branch.getBranchId();

        String branchName =
                hasText(branch.getBranchName())
                        ? branch.getBranchName()
                        : "school";

        String location =
                hasText(branch.getBranchLocation())
                        ? branch.getBranchLocation()
                        : null;

        return location == null
                ? schoolCode
                + "-"
                + branchName
                : schoolCode
                + "-"
                + branchName
                + ","
                + location;
    }

    private void requireEmployeeStorageIdentity(
            ErpEmployee employee
    ) {
        Objects.requireNonNull(
                employee,
                "Employee entity is required."
        );

        if (employee.getBranch() == null) {
            throw new IllegalStateException(
                    "Employee branch is required before storing files."
            );
        }

        if (!hasText(employee.getEmployeeNo())) {
            throw new IllegalStateException(
                    "Employee number is required before storing files."
            );
        }

        if (!hasText(employee.getFullName())) {
            throw new IllegalStateException(
                    "Employee full name is required before storing files."
            );
        }
    }

    private ErpEmployee requireNestedEmployee(
            ErpEmployee employee
    ) {
        requireEmployeeStorageIdentity(
                employee
        );

        return employee;
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
                        || Files.isSymbolicLink(
                        filePath
                )
        ) {
            throw new ResourceNotFoundException(
                    "Employee file was not found."
            );
        }

        return filePath;
    }

    private Path resolvePathInsideRoot(
            String relativePath
    ) {
        if (!hasText(relativePath)) {
            throw new ResourceNotFoundException(
                    "Employee file was not found."
            );
        }

        Path suppliedPath =
                Path.of(relativePath.trim());

        if (suppliedPath.isAbsolute()) {
            throw new SecurityException(
                    "Absolute Employee file paths are not allowed."
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

        if (
                !normalizedPath.startsWith(
                        privateStorageRoot
                )
        ) {
            throw new SecurityException(
                    "Invalid Employee storage path."
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
                privateStorageRoot
                        .relativize(
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
                            && Files.isSymbolicLink(
                            current
                    )
            ) {
                throw new SecurityException(
                        "Symbolic links are not allowed in Employee storage."
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
        try (
                InputStream inputStream =
                        Files.newInputStream(
                                filePath
                        )
        ) {
            return inputStream.readNBytes(
                    16
            );
        }
    }

    // =====================================================================
    // SAFE NAMES AND PREFIXES
    // =====================================================================

    private String buildQualificationPrefix(
            ErpEmployeeQualification qualification
    ) {
        String qualificationName =
                firstNonBlank(
                        qualification.getEmployeeQualificationName(),
                        qualification
                                .getEmployeeQualificationLevel() == null
                                ? null
                                : qualification
                                .getEmployeeQualificationLevel()
                                .name()
                );

        return "qualification-"
                + firstNonBlank(
                qualificationName,
                "certificate"
        );
    }

    private String buildExperiencePrefix(
            String prefix,
            ErpEmployeeExperience experience
    ) {
        return prefix
                + "-"
                + firstNonBlank(
                experience.getEmployeeExperienceCompanyName(),
                "organisation"
        );
    }

    private String buildDocumentPrefix(
            ErpEmployeeDocument document
    ) {
        String type =
                document.getEmployeeDocumentType() == null
                        ? "document"
                        : document.getEmployeeDocumentType()
                        .name();

        return "document-"
                + type
                + "-"
                + firstNonBlank(
                document.getEmployeeDocumentName(),
                "file"
        );
    }

    private String firstNonBlank(
            String first,
            String second
    ) {
        return hasText(first)
                ? first
                : second;
    }


    private String sanitizePathSegment(
            String value
    ) {
        if (!hasText(value)) {
            return "unknown";
        }

        String sanitized =
                value.trim()
                        .replaceAll(
                                "[^a-zA-Z0-9.\\- ,_]",
                                "_"
                        )
                        .replaceAll(
                                "\\s+",
                                " "
                        )
                        .replaceAll(
                                "_+",
                                "_"
                        );

        sanitized =
                trimUnsafePathEdges(
                        sanitized
                );

        if (sanitized.length() > 150) {
            sanitized =
                    sanitized.substring(
                            0,
                            150
                    );
        }

        return sanitized.isBlank()
                ? "unknown"
                : sanitized;
    }

    private String sanitizeFilePrefix(
            String value
    ) {
        if (!hasText(value)) {
            return "employee-file";
        }

        String sanitized =
                value.trim()
                        .toLowerCase(
                                Locale.ROOT
                        )
                        .replaceAll(
                                "[^a-z0-9._-]",
                                "-"
                        )
                        .replaceAll(
                                "-+",
                                "-"
                        )
                        .replaceAll(
                                "^[-._]+|[-._]+$",
                                ""
                        );

        if (sanitized.length() > 80) {
            sanitized =
                    sanitized.substring(
                            0,
                            80
                    );
        }

        return sanitized.isBlank()
                ? "employee-file"
                : sanitized;
    }

    private String sanitizeOriginalFilename(
            String originalFilename
    ) {
        if (!hasText(originalFilename)) {
            return "employee-file";
        }

        String filename =
                extractFilename(
                        originalFilename
                );

        if (filename.length() > 255) {
            filename =
                    filename.substring(
                            filename.length() - 255
                    );
        }

        return filename.isBlank()
                ? "employee-file"
                : filename;
    }

    private String extractFilename(
            String originalFilename
    ) {
        String normalizedName =
                originalFilename
                        .replace('\\', '/');

        int lastSeparator =
                normalizedName.lastIndexOf('/');

        return normalizedName
                .substring(
                        lastSeparator + 1
                )
                .trim()
                .replaceAll(
                        "\\p{Cntrl}",
                        ""
                );
    }

    private String trimUnsafePathEdges(
            String value
    ) {
        String result =
                value;

        while (
                result.startsWith(".")
                        || result.startsWith(" ")
        ) {
            result =
                    result.substring(1);
        }

        while (
                result.endsWith(".")
                        || result.endsWith(" ")
        ) {
            result =
                    result.substring(
                            0,
                            result.length() - 1
                    );
        }

        return result;
    }

    private List<String> normalizeStoredPaths(
            Collection<String> relativePaths
    ) {
        if (
                relativePaths == null
                        || relativePaths.isEmpty()
        ) {
            return List.of();
        }

        Set<String> uniquePaths =
                new LinkedHashSet<>();

        for (String relativePath : relativePaths) {
            if (hasText(relativePath)) {
                uniquePaths.add(
                        relativePath.trim()
                );
            }
        }

        return List.copyOf(
                uniquePaths
        );
    }

    private int toMegabytes(
            long bytes
    ) {
        return Math.toIntExact(
                bytes / ONE_MEGABYTE
        );
    }

    private boolean hasText(
            String value
    ) {
        return StringUtils.hasText(value);
    }

    // =====================================================================
    // INTERNAL IMMUTABLE VALUES
    // =====================================================================

    private record ParsedBase64(
            String mimeType,
            String payload
    ) {
    }

    private record DecodedUpload(
            byte[] bytes,
            String mimeType,
            String extension
    ) {
    }

    public record StoredFile(
            String relativePath,
            String storedFileName,
            String originalFileName,
            String mimeType,
            String extension,
            long size
    ) {
    }
}
