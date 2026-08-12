package com.erp.montfortuganda.admission.service;

import com.erp.montfortuganda.admission.dto.PublicDocumentUploadInfoDTO;
import com.erp.montfortuganda.admission.dto.PublicDocumentUploadResponseDTO;
import com.erp.montfortuganda.admission.entity.ErpApplication;
import com.erp.montfortuganda.admission.entity.ErpApplicationDocument;
import com.erp.montfortuganda.admission.entity.ErpApplicationDocumentRequest;
import com.erp.montfortuganda.admission.entity.ErpApplicationStatusHistory;
import com.erp.montfortuganda.admission.repository.ErpApplicationDocumentRepository;
import com.erp.montfortuganda.admission.repository.ErpApplicationDocumentRequestRepository;
import com.erp.montfortuganda.admission.repository.ErpApplicationRepository;
import com.erp.montfortuganda.admission.repository.ErpApplicationStatusHistoryRepository;
import com.erp.montfortuganda.exception.BadRequestException;
import com.erp.montfortuganda.exception.ResourceNotFoundException;
import com.erp.montfortuganda.infrastructure.enums.DocumentType;
import com.erp.montfortuganda.infrastructure.service.StorageService;
import com.erp.montfortuganda.school.entity.Branch;
import com.erp.montfortuganda.school.service.FileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;

/**
 * Public, token-protected upload flow for documents requested during
 * admission verification.
 *
 * <p>The raw token is never stored or logged. Database lookup uses its
 * SHA-256 hash, and the upload transaction locks the request so the same
 * token cannot be consumed concurrently.</p>
 */
@Service
public class PublicDocumentUploadService {

    private static final String HISTORY_STAGE =
            "DOCUMENT_VERIFICATION";

    private static final String TRANSITION_SOURCE =
            "PUBLIC_PORTAL";

    private static final String EMAIL_NOT_REQUIRED =
            "NOT_REQUIRED";

    private static final int MIN_TOKEN_LENGTH = 20;
    private static final int MAX_TOKEN_LENGTH = 512;
    private static final int MAX_ORIGINAL_FILE_NAME_LENGTH = 255;

    private static final String PDF_CONTENT_TYPE =
            "application/pdf";

    private static final String JPEG_CONTENT_TYPE =
            "image/jpeg";

    private static final String PNG_CONTENT_TYPE =
            "image/png";

    private final ErpApplicationDocumentRequestRepository
            requestRepository;

    private final ErpApplicationDocumentRepository
            documentRepository;

    private final ErpApplicationRepository
            applicationRepository;

    private final ErpApplicationStatusHistoryRepository
            historyRepository;

    private final StorageService storageService;

    private final FileStorageService branchFileStorageService;

    private final long maximumFileSizeBytes;

    public PublicDocumentUploadService(
            ErpApplicationDocumentRequestRepository requestRepository,
            ErpApplicationDocumentRepository documentRepository,
            ErpApplicationRepository applicationRepository,
            ErpApplicationStatusHistoryRepository historyRepository,
            StorageService storageService,
            FileStorageService branchFileStorageService,
            @Value(
                    "${erp.admission.requested-document-max-bytes:10485760}"
            )
            long maximumFileSizeBytes
    ) {
        if (maximumFileSizeBytes <= 0) {
            throw new IllegalArgumentException(
                    "Requested-document maximum file size must be greater than zero."
            );
        }

        this.requestRepository = requestRepository;
        this.documentRepository = documentRepository;
        this.applicationRepository = applicationRepository;
        this.historyRepository = historyRepository;
        this.storageService = storageService;
        this.branchFileStorageService =
                branchFileStorageService;
        this.maximumFileSizeBytes = maximumFileSizeBytes;
    }

    /**
     * Returns applicant-safe information for one secure upload link.
     */
    @Transactional(readOnly = true)
    public PublicDocumentUploadInfoDTO getUploadInfo(
            String rawToken
    ) {
        String tokenHash = hashValidatedToken(rawToken);

        ErpApplicationDocumentRequest request =
                requestRepository
                        .findByUploadTokenHashAndActiveTrue(tokenHash)
                        .orElseThrow(
                                () -> invalidLink()
                        );

        return toUploadInfo(
                request,
                LocalDateTime.now()
        );
    }

    /**
     * Loads the selected branch's private logo using the same secure upload
     * token. The stored private path is never returned to the applicant.
     */
    @Transactional(readOnly = true)
    public PublicSchoolLogoResource loadSchoolLogo(
            String rawToken
    ) {
        String tokenHash =
                hashValidatedToken(rawToken);

        ErpApplicationDocumentRequest request =
                requestRepository
                        .findByUploadTokenHashAndActiveTrue(
                                tokenHash
                        )
                        .orElseThrow(
                                this::invalidLink
                        );

        ErpApplication application =
                request.getApplication();

        Branch branch =
                application == null
                        ? null
                        : application.getBranch();

        if (branch == null
                || !StringUtils.hasText(
                        branch.getBranchLogoUrl()
                )) {
            throw new ResourceNotFoundException(
                    "The school logo is not available."
            );
        }

        String relativeLogoPath =
                branch.getBranchLogoUrl()
                        .trim();

        Resource logoResource;

        try {
            logoResource =
                    branchFileStorageService
                            .loadPrivateFile(
                                    relativeLogoPath
                            );
        } catch (IllegalArgumentException exception) {
            throw new ResourceNotFoundException(
                    "The school logo is not available."
            );
        }

        String contentType =
                branchFileStorageService
                        .detectContentType(
                                relativeLogoPath
                        );

        if (!StringUtils.hasText(contentType)
                || !contentType
                        .toLowerCase(Locale.ROOT)
                        .startsWith("image/")) {
            throw new ResourceNotFoundException(
                    "The school logo is not available."
            );
        }

        String fileName =
                Path.of(relativeLogoPath)
                        .getFileName()
                        .toString();

        return new PublicSchoolLogoResource(
                logoResource,
                contentType,
                fileName
        );
    }

    /**
     * Accepts exactly one PDF, JPEG or PNG document for one pending,
     * active and unused request.
     */
    @Transactional
    public PublicDocumentUploadResponseDTO uploadRequestedDocument(
            String rawToken,
            MultipartFile file
    ) {
        String tokenHash = hashValidatedToken(rawToken);

        ErpApplicationDocumentRequest request =
                requestRepository
                        .findByUploadTokenHashForUpdate(tokenHash)
                        .orElseThrow(
                                () -> invalidLink()
                        );

        LocalDateTime now = LocalDateTime.now();

        validateRequestForUpload(
                request,
                now
        );

        if (documentRepository
                .existsByDocumentRequest_RequestIdAndActiveTrue(
                        request.getRequestId()
                )) {
            throw new BadRequestException(
                    "A document has already been uploaded for this request."
            );
        }

        ValidatedUpload validatedUpload =
                validateUploadedFile(file);

        ErpApplication application =
                request.getApplication();

        validateApplicationForUpload(application);

        Branch branch = application.getBranch();

        String branchFolder =
                buildBranchFolder(branch);

        String relativeStoredPath = null;

        try {
            relativeStoredPath =
                    storageService.storeEntityDocument(
                            file,
                            "applications",
                            branchFolder,
                            application.getApplicationNo(),
                            DocumentType.OTHER
                    );

            ErpApplication.DocumentStatus previousDocumentStatus =
                    application.getDocumentStatus();

            ErpApplicationDocument uploadedDocument =
                    buildUploadedDocument(
                            request,
                            application,
                            relativeStoredPath,
                            validatedUpload,
                            now
                    );

            uploadedDocument =
                    documentRepository.saveAndFlush(
                            uploadedDocument
                    );

            supersedeReplacedDocumentIfPresent(
                    application,
                    request,
                    uploadedDocument,
                    now
            );

            request.setRequestStatus(
                    ErpApplicationDocumentRequest
                            .RequestStatus.UPLOADED
            );
            request.setTokenUsedAt(now);
            request.setUpdatedAt(now);

            requestRepository.saveAndFlush(request);

            ErpApplication.DocumentStatus recalculatedStatus =
                    recalculateApplicationDocumentStatus(
                            application,
                            branch.getBranchId()
                    );

            saveUploadHistory(
                    application,
                    request,
                    uploadedDocument,
                    previousDocumentStatus,
                    recalculatedStatus
            );

            return toUploadResponse(
                    request,
                    uploadedDocument,
                    application,
                    branch
            );

        } catch (RuntimeException exception) {
            deleteStoredFileQuietly(
                    relativeStoredPath,
                    exception
            );

            throw exception;
        }
    }

    private PublicDocumentUploadInfoDTO toUploadInfo(
            ErpApplicationDocumentRequest request,
            LocalDateTime now
    ) {
        PublicDocumentUploadInfoDTO response =
                new PublicDocumentUploadInfoDTO();

        ErpApplication application =
                request.getApplication();

        Branch branch =
                application == null
                        ? null
                        : application.getBranch();

        response.setApplicationNo(
                application == null
                        ? null
                        : application.getApplicationNo()
        );

        response.setStudentName(
                buildStudentName(application)
        );

        response.setSchoolName(
                branch == null
                        ? null
                        : branch.getBranchName()
        );

        response.setSchoolCode(
                branch == null
                        ? null
                        : branch.getSchoolCode()
        );

        response.setRequestedDocumentType(
                request.getRequestedDocumentType()
        );

        response.setRequestedDocumentName(
                request.getRequestedDocumentName()
        );

        response.setRequestReason(
                request.getRequestReason()
        );

        response.setPublicRemarks(
                request.getPublicRemarks()
        );

        response.setRequestStatus(
                request.getRequestStatus()
        );

        response.setRequestedAt(
                request.getRequestedAt()
        );

        response.setUploadDeadline(
                request.getUploadDeadline()
        );

        response.setUploadTokenExpiresAt(
                request.getUploadTokenExpiresAt()
        );

        String unavailableReason =
                getUploadUnavailableReason(
                        request,
                        application,
                        now
                );

        response.setUploadAllowed(
                unavailableReason == null
        );

        response.setUploadUnavailableReason(
                unavailableReason
        );

        return response;
    }

    private PublicDocumentUploadResponseDTO toUploadResponse(
            ErpApplicationDocumentRequest request,
            ErpApplicationDocument document,
            ErpApplication application,
            Branch branch
    ) {
        PublicDocumentUploadResponseDTO response =
                new PublicDocumentUploadResponseDTO();

        response.setApplicationNo(
                application.getApplicationNo()
        );

        response.setStudentName(
                buildStudentName(application)
        );

        response.setSchoolName(
                branch.getBranchName()
        );

        response.setRequestedDocumentName(
                request.getRequestedDocumentName()
        );

        response.setUploadedFileName(
                document.getOriginalFileName()
        );

        response.setContentType(
                document.getContentType()
        );

        response.setFileSize(
                document.getFileSize()
        );

        response.setUploadedAt(
                document.getUploadedAt()
        );

        response.setRequestStatus(
                request.getRequestStatus()
        );

        response.setVerificationStatus(
                document.getVerificationStatus()
        );

        response.setMessage(
                "Document uploaded successfully. "
                        + "The school will review it and update the application."
        );

        return response;
    }

    private void validateRequestForUpload(
            ErpApplicationDocumentRequest request,
            LocalDateTime now
    ) {
        String unavailableReason =
                getUploadUnavailableReason(
                        request,
                        request.getApplication(),
                        now
                );

        if (unavailableReason != null) {
            throw new BadRequestException(
                    unavailableReason
            );
        }
    }

    private String getUploadUnavailableReason(
            ErpApplicationDocumentRequest request,
            ErpApplication application,
            LocalDateTime now
    ) {
        if (!Boolean.TRUE.equals(request.getActive())) {
            return "This document upload request is no longer active.";
        }

        if (request.getRequestStatus()
                == ErpApplicationDocumentRequest.RequestStatus.UPLOADED) {
            return "The requested document has already been uploaded.";
        }

        if (request.getRequestStatus()
                == ErpApplicationDocumentRequest.RequestStatus.COMPLETED) {
            return "This document request has already been completed.";
        }

        if (request.getRequestStatus()
                == ErpApplicationDocumentRequest.RequestStatus.CANCELLED) {
            return "This document request was cancelled by the school.";
        }

        if (request.getRequestStatus()
                == ErpApplicationDocumentRequest.RequestStatus.EXPIRED) {
            return "This document upload link has expired.";
        }

        if (request.getRequestStatus()
                != ErpApplicationDocumentRequest.RequestStatus.PENDING) {
            return "This document upload request is unavailable.";
        }

        if (request.getTokenUsedAt() != null) {
            return "This document upload link has already been used.";
        }

        if (request.getUploadTokenExpiresAt() != null
                && !request.getUploadTokenExpiresAt().isAfter(now)) {
            return "This document upload link has expired.";
        }

        if (request.getUploadDeadline() != null
                && !request.getUploadDeadline().isAfter(now)) {
            return "The document upload deadline has passed.";
        }

        if (application == null
                || application.getStatus() == null
                || application.getStatus() != 1) {
            return "This admission application is unavailable.";
        }

        if (Boolean.TRUE.equals(application.getWorkflowLocked())
                || application.getCurrentStage()
                == ErpApplication.CurrentStage.ENROLLED
                || application.getCurrentStage()
                == ErpApplication.CurrentStage.CLOSED
                || application.getAdmissionStatus()
                == ErpApplication.AdmissionStatus.ENROLLED
                || application.getAdmissionStatus()
                == ErpApplication.AdmissionStatus.CLOSED) {
            return "This admission application no longer accepts documents.";
        }

        if (documentRepository
                .existsByDocumentRequest_RequestIdAndActiveTrue(
                        request.getRequestId()
                )) {
            return "A document has already been uploaded for this request.";
        }

        return null;
    }

    private void validateApplicationForUpload(
            ErpApplication application
    ) {
        if (application == null
                || application.getBranch() == null) {
            throw invalidLink();
        }

        if (application.getStatus() == null
                || application.getStatus() != 1) {
            throw new BadRequestException(
                    "This admission application is unavailable."
            );
        }

        if (Boolean.TRUE.equals(application.getWorkflowLocked())
                || application.getAdmissionStatus()
                == ErpApplication.AdmissionStatus.ENROLLED
                || application.getAdmissionStatus()
                == ErpApplication.AdmissionStatus.CLOSED) {
            throw new BadRequestException(
                    "This admission application no longer accepts documents."
            );
        }
    }

    private ValidatedUpload validateUploadedFile(
            MultipartFile file
    ) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException(
                    "Please select a document to upload."
            );
        }

        if (file.getSize() <= 0) {
            throw new BadRequestException(
                    "The selected document is empty."
            );
        }

        if (file.getSize() > maximumFileSizeBytes) {
            throw new BadRequestException(
                    "The document exceeds the maximum permitted file size."
            );
        }

        byte[] bytes;

        try {
            bytes = file.getBytes();
        } catch (IOException exception) {
            throw new BadRequestException(
                    "The selected document could not be read."
            );
        }

        if (bytes.length == 0) {
            throw new BadRequestException(
                    "The selected document is empty."
            );
        }

        String detectedContentType =
                detectAllowedContentType(bytes);

        String originalFileName =
                sanitizeOriginalFileName(
                        file.getOriginalFilename()
                );

        validateFileExtension(
                originalFileName,
                detectedContentType
        );

        return new ValidatedUpload(
                originalFileName,
                detectedContentType,
                (long) bytes.length,
                sha256Hex(bytes)
        );
    }

    private String detectAllowedContentType(
            byte[] bytes
    ) {
        if (isPdf(bytes)) {
            return PDF_CONTENT_TYPE;
        }

        if (isJpeg(bytes)) {
            return JPEG_CONTENT_TYPE;
        }

        if (isPng(bytes)) {
            return PNG_CONTENT_TYPE;
        }

        throw new BadRequestException(
                "Only PDF, JPG, JPEG, and PNG documents are allowed."
        );
    }

    private boolean isPdf(
            byte[] bytes
    ) {
        return bytes.length >= 5
                && bytes[0] == '%'
                && bytes[1] == 'P'
                && bytes[2] == 'D'
                && bytes[3] == 'F'
                && bytes[4] == '-';
    }

    private boolean isJpeg(
            byte[] bytes
    ) {
        return bytes.length >= 3
                && (bytes[0] & 0xFF) == 0xFF
                && (bytes[1] & 0xFF) == 0xD8
                && (bytes[2] & 0xFF) == 0xFF;
    }

    private boolean isPng(
            byte[] bytes
    ) {
        int[] signature = {
                0x89,
                0x50,
                0x4E,
                0x47,
                0x0D,
                0x0A,
                0x1A,
                0x0A
        };

        if (bytes.length < signature.length) {
            return false;
        }

        for (int index = 0;
             index < signature.length;
             index++) {
            if ((bytes[index] & 0xFF)
                    != signature[index]) {
                return false;
            }
        }

        return true;
    }

    private void validateFileExtension(
            String originalFileName,
            String detectedContentType
    ) {
        String lowerName =
                originalFileName.toLowerCase(Locale.ROOT);

        boolean validExtension =
                switch (detectedContentType) {
                    case PDF_CONTENT_TYPE ->
                            lowerName.endsWith(".pdf");

                    case JPEG_CONTENT_TYPE ->
                            lowerName.endsWith(".jpg")
                                    || lowerName.endsWith(".jpeg");

                    case PNG_CONTENT_TYPE ->
                            lowerName.endsWith(".png");

                    default -> false;
                };

        if (!validExtension) {
            throw new BadRequestException(
                    "The document extension does not match its file content."
            );
        }
    }

    private ErpApplicationDocument buildUploadedDocument(
            ErpApplicationDocumentRequest request,
            ErpApplication application,
            String relativeStoredPath,
            ValidatedUpload validatedUpload,
            LocalDateTime now
    ) {
        ErpApplicationDocument document =
                new ErpApplicationDocument();

        document.setApplication(application);
        document.setDocumentRequest(request);
        document.setDocumentType(
                resolveDocumentType(
                        request.getRequestedDocumentType()
                )
        );
        document.setSubmissionSource(
                ErpApplicationDocument
                        .SubmissionSource.PUBLIC_PORTAL
        );
        document.setVerificationStatus(
                ErpApplicationDocument
                        .VerificationStatus.PENDING
        );
        document.setCurrent(true);
        document.setOriginalFileName(
                validatedUpload.originalFileName()
        );
        document.setStoredFileName(
                Path.of(relativeStoredPath)
                        .getFileName()
                        .toString()
        );
        document.setFilePath(
                relativeStoredPath
        );
        document.setUploadedAt(now);
        document.setUpdatedAt(now);
        document.setFileSize(
                validatedUpload.fileSize()
        );
        document.setContentType(
                validatedUpload.contentType()
        );
        document.setFileHash(
                validatedUpload.fileHash()
        );
        document.setUploadedBy(null);
        document.setActive(true);

        return document;
    }

    private void supersedeReplacedDocumentIfPresent(
            ErpApplication application,
            ErpApplicationDocumentRequest request,
            ErpApplicationDocument replacement,
            LocalDateTime now
    ) {
        ErpApplicationDocument.DocumentType requestedType =
                resolveDocumentType(
                        request.getRequestedDocumentType()
                );

        List<ErpApplicationDocument> reuploadDocuments =
                documentRepository
                        .findAllByApplication_ApplicationIdAndApplication_Branch_BranchIdAndVerificationStatusAndCurrentTrueAndActiveTrueOrderByUploadedAtAsc(
                                application.getApplicationId(),
                                application.getBranch().getBranchId(),
                                ErpApplicationDocument
                                        .VerificationStatus.REUPLOAD_REQUIRED
                        );

        List<ErpApplicationDocument> matchedDocuments =
                reuploadDocuments.stream()
                        .filter(
                                existing ->
                                        existing.getDocumentType()
                                                == requestedType
                        )
                        .toList();

        if (matchedDocuments.isEmpty()) {
            return;
        }

        for (ErpApplicationDocument existing
                : matchedDocuments) {
            existing.setCurrent(false);
            existing.setVerificationStatus(
                    ErpApplicationDocument
                            .VerificationStatus.SUPERSEDED
            );
            existing.setReplacementDocument(
                    replacement
            );
            existing.setSupersededAt(now);
            existing.setSupersededByUserId(null);
            existing.setUpdatedAt(now);
        }

        documentRepository.saveAllAndFlush(
                matchedDocuments
        );
    }

    private ErpApplication.DocumentStatus
    recalculateApplicationDocumentStatus(
            ErpApplication application,
            Integer branchId
    ) {
        Long applicationId =
                application.getApplicationId();

        long pendingRequests =
                requestRepository
                        .countByApplication_ApplicationIdAndApplication_Branch_BranchIdAndRequestStatusAndActiveTrue(
                                applicationId,
                                branchId,
                                ErpApplicationDocumentRequest
                                        .RequestStatus.PENDING
                        );

        long totalDocuments =
                documentRepository
                        .countByApplication_ApplicationIdAndApplication_Branch_BranchIdAndCurrentTrueAndActiveTrue(
                                applicationId,
                                branchId
                        );

        long reuploadRequired =
                documentRepository
                        .countByApplication_ApplicationIdAndApplication_Branch_BranchIdAndVerificationStatusAndCurrentTrueAndActiveTrue(
                                applicationId,
                                branchId,
                                ErpApplicationDocument
                                        .VerificationStatus.REUPLOAD_REQUIRED
                        );

        long rejected =
                documentRepository
                        .countByApplication_ApplicationIdAndApplication_Branch_BranchIdAndVerificationStatusAndCurrentTrueAndActiveTrue(
                                applicationId,
                                branchId,
                                ErpApplicationDocument
                                        .VerificationStatus.REJECTED
                        );

        long verified =
                documentRepository
                        .countByApplication_ApplicationIdAndApplication_Branch_BranchIdAndVerificationStatusAndCurrentTrueAndActiveTrue(
                                applicationId,
                                branchId,
                                ErpApplicationDocument
                                        .VerificationStatus.VERIFIED
                        );

        ErpApplication.DocumentStatus status;

        if (pendingRequests > 0
                || reuploadRequired > 0) {
            status =
                    ErpApplication.DocumentStatus
                            .REUPLOAD_REQUIRED;

            application.setVerificationStatus(
                    ErpApplication.VerificationStatus
                            .ADDITIONAL_DOCUMENTS_REQUIRED
            );
        } else if (rejected > 0) {
            status =
                    ErpApplication.DocumentStatus.REJECTED;
        } else if (totalDocuments > 0
                && verified == totalDocuments) {
            status =
                    ErpApplication.DocumentStatus.VERIFIED;

            if (isAfterApplicationVerificationStage(application)) {
                application.setVerificationStatus(
                        ErpApplication.VerificationStatus.APPROVED
                );
            } else if (application.getVerificationStatus()
                    == ErpApplication.VerificationStatus
                    .ADDITIONAL_DOCUMENTS_REQUIRED) {
                application.setVerificationStatus(
                        ErpApplication.VerificationStatus.PENDING
                );
            }
        } else {
            status =
                    ErpApplication.DocumentStatus.PENDING;

            if (application.getVerificationStatus()
                    == ErpApplication.VerificationStatus
                    .ADDITIONAL_DOCUMENTS_REQUIRED) {
                application.setVerificationStatus(
                        ErpApplication.VerificationStatus.PENDING
                );
            }
        }

        recoverPreviouslyScheduledSchoolVisitStage(application);

        application.setDocumentStatus(status);

        /*
         * A requested-document upload must not rewind the main admission
         * workflow. If the application is already in SCHOOL_VISIT or later,
         * keep that stage and let the pending document state temporarily block
         * the next action until Branch Admin review is completed.
         */
        applicationRepository.saveAndFlush(
                application
        );

        return status;
    }

    /**
     * Self-heals applications that were rolled back to verification by the
     * older requested-document implementation. Existing School Visit schedule
     * data is preserved and the main workflow stage is restored.
     */
    private void recoverPreviouslyScheduledSchoolVisitStage(
            ErpApplication application
    ) {
        if (application == null
                || application.getCurrentStage()
                != ErpApplication.CurrentStage.APPLICATION_VERIFICATION
                || application.getSchoolVisitScheduledAt() == null
                || application.getSchoolVisitStatus() == null) {
            return;
        }

        ErpApplication.SchoolVisitStatus visitStatus =
                application.getSchoolVisitStatus();

        if (visitStatus == ErpApplication.SchoolVisitStatus.SCHEDULED
                || visitStatus == ErpApplication.SchoolVisitStatus.RESCHEDULED
                || visitStatus == ErpApplication.SchoolVisitStatus.ATTENDED
                || visitStatus == ErpApplication.SchoolVisitStatus.COMPLETED) {
            application.setCurrentStage(
                    ErpApplication.CurrentStage.SCHOOL_VISIT
            );
        }
    }

    private boolean isAfterApplicationVerificationStage(
            ErpApplication application
    ) {
        if (application == null
                || application.getCurrentStage() == null) {
            return false;
        }

        return switch (application.getCurrentStage()) {
            case SCHOOL_VISIT,
                 ENTRANCE_TEST,
                 PARENT_FEE_DISCUSSION,
                 PAYMENT,
                 SCHOLARSHIP,
                 FINAL_ADMISSION,
                 ENROLLED,
                 CLOSED -> true;
            case APPLICATION_DRAFT,
                 APPLICATION_VERIFICATION -> false;
        };
    }

    private void saveUploadHistory(
            ErpApplication application,
            ErpApplicationDocumentRequest request,
            ErpApplicationDocument document,
            ErpApplication.DocumentStatus oldStatus,
            ErpApplication.DocumentStatus newStatus
    ) {
        ErpApplicationStatusHistory history =
                new ErpApplicationStatusHistory();

        history.setApplication(application);
        history.setStage(HISTORY_STAGE);
        history.setOldStatus(oldStatus);
        history.setNewStatus(newStatus);
        history.setChangedBy(null);
        history.setPublicRemarks(
                "Requested document uploaded successfully."
        );
        history.setInternalRemarks(
                "Applicant uploaded document request "
                        + request.getRequestId()
                        + " as document "
                        + document.getDocumentId()
                        + "."
        );
        history.setTransitionSource(
                TRANSITION_SOURCE
        );
        history.setEmailRequired(false);
        history.setEmailStatus(
                EMAIL_NOT_REQUIRED
        );

        historyRepository.saveAndFlush(
                history
        );
    }

    private ErpApplicationDocument.DocumentType
    resolveDocumentType(
            String requestedDocumentType
    ) {
        if (!StringUtils.hasText(
                requestedDocumentType
        )) {
            return ErpApplicationDocument
                    .DocumentType.OTHER;
        }

        String normalized =
                requestedDocumentType
                        .trim()
                        .toUpperCase(Locale.ROOT)
                        .replaceAll(
                                "[^A-Z0-9]+",
                                "_"
                        )
                        .replaceAll(
                                "^_+|_+$",
                                ""
                        );

        try {
            return ErpApplicationDocument
                    .DocumentType.valueOf(
                            normalized
                    );
        } catch (IllegalArgumentException exception) {
            return ErpApplicationDocument
                    .DocumentType.OTHER;
        }
    }

    private String buildBranchFolder(
            Branch branch
    ) {
        String schoolCode =
                defaultText(
                        branch.getSchoolCode(),
                        "UNKNOWN"
                );

        String branchName =
                defaultText(
                        branch.getBranchName(),
                        "Branch"
                );

        String branchLocation =
                defaultText(
                        branch.getBranchLocation(),
                        "Location"
                );

        return schoolCode
                + "-"
                + branchName
                + ","
                + branchLocation;
    }

    private String buildStudentName(
            ErpApplication application
    ) {
        if (application == null) {
            return null;
        }

        StringBuilder name =
                new StringBuilder();

        appendName(
                name,
                application.getFirstName()
        );

        appendName(
                name,
                application.getMiddleName()
        );

        appendName(
                name,
                application.getLastName()
        );

        return name.toString();
    }

    private void appendName(
            StringBuilder target,
            String value
    ) {
        if (!StringUtils.hasText(value)) {
            return;
        }

        if (!target.isEmpty()) {
            target.append(' ');
        }

        target.append(value.trim());
    }

    private String sanitizeOriginalFileName(
            String originalFileName
    ) {
        if (!StringUtils.hasText(
                originalFileName
        )) {
            throw new BadRequestException(
                    "The selected document name is invalid."
            );
        }

        String safeName =
                originalFileName
                        .replace('\\', '/');

        int finalSlash =
                safeName.lastIndexOf('/');

        if (finalSlash >= 0) {
            safeName =
                    safeName.substring(
                            finalSlash + 1
                    );
        }

        safeName =
                safeName.replaceAll(
                        "[\\r\\n\\t\\u0000-\\u001F\\u007F]",
                        ""
                ).trim();

        if (!StringUtils.hasText(safeName)) {
            throw new BadRequestException(
                    "The selected document name is invalid."
            );
        }

        if (safeName.length()
                > MAX_ORIGINAL_FILE_NAME_LENGTH) {
            safeName =
                    safeName.substring(
                            safeName.length()
                                    - MAX_ORIGINAL_FILE_NAME_LENGTH
                    );
        }

        return safeName;
    }

    private String hashValidatedToken(
            String rawToken
    ) {
        if (!StringUtils.hasText(rawToken)) {
            throw invalidLink();
        }

        String normalizedToken =
                rawToken.trim();

        if (normalizedToken.length()
                < MIN_TOKEN_LENGTH
                || normalizedToken.length()
                > MAX_TOKEN_LENGTH) {
            throw invalidLink();
        }

        return sha256Hex(
                normalizedToken.getBytes(
                        StandardCharsets.UTF_8
                )
        );
    }

    private String sha256Hex(
            byte[] value
    ) {
        try {
            MessageDigest digest =
                    MessageDigest.getInstance(
                            "SHA-256"
                    );

            return HexFormat.of()
                    .formatHex(
                            digest.digest(value)
                    );
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "SHA-256 is not available.",
                    exception
            );
        }
    }

    private ResourceNotFoundException invalidLink() {
        return new ResourceNotFoundException(
                "The document upload link is invalid or unavailable."
        );
    }

    private String defaultText(
            String value,
            String fallback
    ) {
        return StringUtils.hasText(value)
                ? value.trim()
                : fallback;
    }

    private void deleteStoredFileQuietly(
            String relativeStoredPath,
            RuntimeException originalException
    ) {
        if (!StringUtils.hasText(
                relativeStoredPath
        )) {
            return;
        }

        try {
            storageService.deleteStoredFile(
                    relativeStoredPath,
                    false
            );
        } catch (RuntimeException cleanupException) {
            originalException.addSuppressed(
                    cleanupException
            );
        }
    }

    public record PublicSchoolLogoResource(
            Resource resource,
            String contentType,
            String fileName
    ) {
    }

    private record ValidatedUpload(
            String originalFileName,
            String contentType,
            Long fileSize,
            String fileHash
    ) {
    }
}
