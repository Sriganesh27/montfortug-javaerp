package com.erp.montfortuganda.admission.service;

import com.erp.montfortuganda.admission.dto.ApplicationDocumentDeleteRequestDTO;
import com.erp.montfortuganda.admission.dto.ApplicationDocumentRequestCancelDTO;
import com.erp.montfortuganda.admission.dto.ApplicationDocumentRequestCreateDTO;
import com.erp.montfortuganda.admission.dto.ApplicationDocumentRequestResponseDTO;
import com.erp.montfortuganda.admission.dto.ApplicationDocumentResponseDTO;
import com.erp.montfortuganda.admission.dto.ApplicationDocumentReviewRequestDTO;
import com.erp.montfortuganda.admission.entity.ErpApplication;
import com.erp.montfortuganda.admission.entity.ErpApplicationDocument;
import com.erp.montfortuganda.admission.entity.ErpApplicationDocumentRequest;
import com.erp.montfortuganda.admission.entity.ErpApplicationStatusHistory;
import com.erp.montfortuganda.admission.mapper.ApplicationDocumentMapper;
import com.erp.montfortuganda.admission.repository.ErpApplicationDocumentRepository;
import com.erp.montfortuganda.admission.repository.ErpApplicationDocumentRequestRepository;
import com.erp.montfortuganda.admission.repository.ErpApplicationRepository;
import com.erp.montfortuganda.admission.repository.ErpApplicationStatusHistoryRepository;
import com.erp.montfortuganda.auth.service.BranchAccessService;
import com.erp.montfortuganda.auth.service.CurrentUserContext;
import com.erp.montfortuganda.exception.BadRequestException;
import com.erp.montfortuganda.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;

/**
 * Branch-protected admission document review and request service.
 *
 * <p>All browser-supplied application, document and request identifiers are
 * resolved together with the authenticated branch. Physical paths and token
 * hashes are never returned through DTOs.</p>
 */
@Service
public class ApplicationDocumentServiceImpl
        implements ApplicationDocumentService {

    private static final String DOCUMENT_STAGE =
            "DOCUMENT_VERIFICATION";

    private static final String TRANSITION_SOURCE =
            "ERP";

    private static final String EMAIL_PENDING =
            "PENDING";

    private static final int TOKEN_BYTE_LENGTH =
            32;

    private static final int TOKEN_CREATION_ATTEMPTS =
            5;

    private final ErpApplicationRepository applicationRepository;
    private final ErpApplicationDocumentRepository documentRepository;
    private final ErpApplicationDocumentRequestRepository requestRepository;
    private final ErpApplicationStatusHistoryRepository historyRepository;
    private final ApplicationDocumentMapper documentMapper;
    private final BranchAccessService branchAccessService;
    private final ApplicationEventPublisher applicationEventPublisher;

    private final Path publicUploadRoot;
    private final long uploadTokenValidityHours;

    private final SecureRandom secureRandom =
            new SecureRandom();

    public ApplicationDocumentServiceImpl(
            ErpApplicationRepository applicationRepository,
            ErpApplicationDocumentRepository documentRepository,
            ErpApplicationDocumentRequestRepository requestRepository,
            ErpApplicationStatusHistoryRepository historyRepository,
            ApplicationDocumentMapper documentMapper,
            BranchAccessService branchAccessService,
            ApplicationEventPublisher applicationEventPublisher,
            @Value("${erp.storage.location:uploads}")
            String publicUploadLocation,
            @Value("${erp.admission.document-upload-token-hours:168}")
            long uploadTokenValidityHours
    ) {
        this.applicationRepository = applicationRepository;
        this.documentRepository = documentRepository;
        this.requestRepository = requestRepository;
        this.historyRepository = historyRepository;
        this.documentMapper = documentMapper;
        this.branchAccessService = branchAccessService;
        this.applicationEventPublisher =
                applicationEventPublisher;

        if (!StringUtils.hasText(publicUploadLocation)) {
            throw new IllegalArgumentException(
                    "Public admission upload location is required."
            );
        }

        if (uploadTokenValidityHours <= 0) {
            throw new IllegalArgumentException(
                    "Document upload-token validity must be greater than zero."
            );
        }

        this.publicUploadRoot =
                Path.of(publicUploadLocation)
                        .toAbsolutePath()
                        .normalize();

        this.uploadTokenValidityHours =
                uploadTokenValidityHours;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApplicationDocumentResponseDTO>
    getApplicationDocuments(
            CurrentUserContext context,
            Long applicationId
    ) {
        Integer branchId =
                requireBranchId(context);

        requireAccessibleApplication(
                applicationId,
                branchId
        );

        /*
         * Return every active document version, including superseded uploads.
         * This lets an authorized Branch Admin identify and delete duplicate,
         * incorrect, replaced, or no-longer-required files to reclaim public
         * upload storage. Inactive audit rows remain hidden.
         */
        return documentRepository
                .findAllByApplication_ApplicationIdAndApplication_Branch_BranchIdAndActiveTrueOrderByUploadedAtDesc(
                        applicationId,
                        branchId
                )
                .stream()
                .map(documentMapper::toDocumentResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ApplicationDocumentResponseDTO
    getApplicationDocument(
            CurrentUserContext context,
            Long applicationId,
            Long documentId
    ) {
        Integer branchId =
                requireBranchId(context);

        ErpApplicationDocument document =
                findAccessibleDocument(
                        applicationId,
                        documentId,
                        branchId
                );

        return documentMapper.toDocumentResponse(
                document
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApplicationDocumentFile
    loadApplicationDocument(
            CurrentUserContext context,
            Long applicationId,
            Long documentId
    ) {
        Integer branchId =
                requireBranchId(context);

        ErpApplicationDocument document =
                findAccessibleDocument(
                        applicationId,
                        documentId,
                        branchId
                );

        Path filePath =
                resolveStoredFile(
                        document.getFilePath()
                );

        try {
            Resource resource =
                    new UrlResource(
                            filePath.toUri()
                    );

            if (!resource.exists()
                    || !resource.isReadable()) {
                throw new ResourceNotFoundException(
                        "Application document file was not found."
                );
            }

            long fileSize =
                    document.getFileSize() != null
                            ? document.getFileSize()
                            : Files.size(filePath);

            return new ApplicationDocumentFile(
                    resource,
                    safeDownloadFileName(
                            document.getOriginalFileName(),
                            document.getDocumentId()
                    ),
                    detectContentType(
                            filePath,
                            document.getContentType()
                    ),
                    fileSize
            );
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Could not read the application document.",
                    exception
            );
        }
    }

    @Override
    @Transactional
    public ApplicationDocumentResponseDTO reviewDocument(
            CurrentUserContext context,
            Long applicationId,
            Long documentId,
            ApplicationDocumentReviewRequestDTO request
    ) {
        requireRequest(
                request,
                "Document review request is required."
        );

        if (request.getDecision() == null) {
            throw new BadRequestException(
                    "Document review decision is required."
            );
        }

        Integer branchId =
                requireBranchId(context);

        Integer userId =
                requireUserId(context);

        ErpApplicationDocument document =
                documentRepository
                        .findCurrentForReview(
                                requirePositiveId(
                                        documentId,
                                        "Document ID"
                                ),
                                requirePositiveId(
                                        applicationId,
                                        "Application ID"
                                ),
                                branchId
                        )
                        .orElseThrow(
                                () -> new ResourceNotFoundException(
                                        "Current application document was not found."
                                )
                        );

        ErpApplication application =
                document.getApplication();

        ensureWorkflowEditable(application);

        ErpApplication.DocumentStatus previousApplicationStatus =
                application.getDocumentStatus();

        LocalDateTime now =
                LocalDateTime.now();

        switch (request.getDecision()) {
            case VERIFY -> verifyDocument(
                    document,
                    userId,
                    now,
                    request
            );

            case REJECT -> rejectDocument(
                    document,
                    userId,
                    now,
                    request
            );

            case REQUEST_REUPLOAD ->
                    requestDocumentReupload(
                            application,
                            document,
                            userId,
                            now,
                            request
                    );
        }

        ErpApplicationDocument savedDocument =
                documentRepository.save(document);

        ErpApplication.DocumentStatus recalculatedStatus =
                recalculateApplicationDocumentStatus(
                        application,
                        branchId,
                        userId
                );

        saveHistory(
                application,
                previousApplicationStatus,
                recalculatedStatus,
                userId,
                request.getPublicRemarks(),
                buildReviewInternalRemarks(
                        savedDocument,
                        request
                ),
                request.getDecision()
                        == ApplicationDocumentReviewRequestDTO
                        .ReviewDecision.REQUEST_REUPLOAD,
                request.getDecision()
                        == ApplicationDocumentReviewRequestDTO
                        .ReviewDecision.REQUEST_REUPLOAD
                        ? "ADDITIONAL_DOCUMENTS_REQUIRED"
                        : null
        );

        return documentMapper.toDocumentResponse(
                savedDocument
        );
    }

    /**
     * Deactivates one unnecessary application document while preserving both
     * the application and document database records for audit.
     *
     * <p>The physical public-upload file is removed only by an AFTER_COMMIT
     * listener. Secure Branch/Admin storage is never accessed by this flow.</p>
     */
    @Override
    @Transactional
    public void deleteDocument(
            CurrentUserContext context,
            Long applicationId,
            Long documentId,
            ApplicationDocumentDeleteRequestDTO request
    ) {
        requireRequest(
                request,
                "Document deletion details are required."
        );

        if (request.getDeletionReason() == null) {
            throw new BadRequestException(
                    "Document deletion reason is required."
            );
        }

        String deletionDetails =
                requireText(
                        request.getDeletionDetails(),
                        "Document deletion details"
                );

        Integer branchId =
                requireBranchId(context);

        Integer userId =
                requireUserId(context);

        ErpApplicationDocument document =
                documentRepository
                        .findActiveForDeletion(
                                requirePositiveId(
                                        documentId,
                                        "Document ID"
                                ),
                                requirePositiveId(
                                        applicationId,
                                        "Application ID"
                                ),
                                branchId
                        )
                        .orElseThrow(
                                () -> new ResourceNotFoundException(
                                        "Active application document was not found."
                                )
                        );

        ErpApplication application =
                document.getApplication();

        ensureWorkflowEditable(application);

        ErpApplication.DocumentStatus previousApplicationStatus =
                application.getDocumentStatus();

        boolean wasCurrent =
                Boolean.TRUE.equals(
                        document.getCurrent()
                );

        LocalDateTime now =
                LocalDateTime.now();

        String deletionAuditRemarks =
                buildDocumentDeletionInternalRemarks(
                        document,
                        request.getDeletionReason(),
                        deletionDetails
                );

        document.setActive(false);
        document.setCurrent(false);
        document.setVerificationStatus(
                ErpApplicationDocument
                        .VerificationStatus.SUPERSEDED
        );
        document.setSupersededAt(now);
        document.setSupersededByUserId(userId);
        document.setInternalRemarks(
                appendAuditRemark(
                        document.getInternalRemarks(),
                        deletionAuditRemarks
                )
        );

        ErpApplicationDocument savedDocument =
                documentRepository.saveAndFlush(
                        document
                );

        clearApplicationPhotoReferenceWhenDeleted(
                application,
                savedDocument
        );

        ErpApplication.DocumentStatus recalculatedStatus =
                recalculateApplicationDocumentStatus(
                        application,
                        branchId,
                        userId
                );

        synchronizeWorkflowAfterDocumentDeletion(
                application,
                wasCurrent,
                recalculatedStatus,
                userId
        );

        saveHistory(
                application,
                previousApplicationStatus,
                recalculatedStatus,
                userId,
                null,
                deletionAuditRemarks,
                false,
                null
        );

        applicationEventPublisher.publishEvent(
                new ApplicationDocumentFileDeleteRequestedEvent(
                        savedDocument.getDocumentId()
                )
        );
    }

    @Override
    @Transactional
    public ApplicationDocumentRequestResponseDTO
    createDocumentRequest(
            CurrentUserContext context,
            Long applicationId,
            ApplicationDocumentRequestCreateDTO request
    ) {
        requireRequest(
                request,
                "Additional-document request is required."
        );

        Integer branchId =
                requireBranchId(context);

        Integer userId =
                requireUserId(context);

        ErpApplication application =
                requireAccessibleApplication(
                        applicationId,
                        branchId
                );

        ensureWorkflowEditable(application);

        String documentType =
                normalizeDocumentType(
                        request.getRequestedDocumentType()
                );

        String documentName =
                resolveRequestedDocumentName(
                        request.getRequestedDocumentName(),
                        documentType
                );

        String requestReason =
                resolveRequestReason(
                        request.getRequestReason()
                );

        validateFutureDeadline(
                request.getUploadDeadline()
        );

        preventDuplicatePendingRequest(
                applicationId,
                branchId,
                documentType
        );

        ErpApplication.DocumentStatus previousStatus =
                application.getDocumentStatus();

        LocalDateTime now =
                LocalDateTime.now();

        GeneratedToken generatedToken =
                generateUniqueUploadToken();

        ErpApplicationDocumentRequest requestRecord =
                buildDocumentRequest(
                        application,
                        documentType,
                        documentName,
                        requestReason,
                        trimToNull(request.getPublicRemarks()),
                        trimToNull(request.getInternalRemarks()),
                        request.getUploadDeadline(),
                        userId,
                        now,
                        generatedToken.hash()
                );

        ErpApplicationDocumentRequest savedRequest =
                requestRepository.save(
                        requestRecord
                );

        application.setDocumentStatus(
                ErpApplication.DocumentStatus.REUPLOAD_REQUIRED
        );
        application.setVerificationStatus(
                ErpApplication.VerificationStatus
                        .ADDITIONAL_DOCUMENTS_REQUIRED
        );

        /*
         * Additional-document requests are orthogonal to the main admission
         * stage. If the application is already in SCHOOL_VISIT or a later
         * stage, do not rewind it to APPLICATION_VERIFICATION. The unresolved
         * document state itself blocks stage-specific progression until the
         * document is resolved.
         */
        application.setUpdatedBy(
                userId.longValue()
        );

        applicationRepository.save(
                application
        );

        saveHistory(
                application,
                previousStatus,
                ErpApplication.DocumentStatus.REUPLOAD_REQUIRED,
                userId,
                savedRequest.getPublicRemarks(),
                "Additional document requested: "
                        + savedRequest.getRequestedDocumentName()
                        + ". Request ID: "
                        + savedRequest.getRequestId(),
                true,
                "ADDITIONAL_DOCUMENTS_REQUIRED"
        );

        /*
         * Publish only the in-memory raw token. The listener runs after this
         * transaction commits, verifies it against the stored SHA-256 hash,
         * sends the email, and updates email_status to SENT or FAILED.
         */
        publishAdditionalDocumentEmail(
                savedRequest,
                generatedToken.raw()
        );

        return documentMapper
                .toDocumentRequestResponse(
                        savedRequest
                );
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApplicationDocumentRequestResponseDTO>
    getDocumentRequests(
            CurrentUserContext context,
            Long applicationId
    ) {
        Integer branchId =
                requireBranchId(context);

        requireAccessibleApplication(
                applicationId,
                branchId
        );

        return requestRepository
                .findAllByApplication_ApplicationIdAndApplication_Branch_BranchIdAndActiveTrueOrderByRequestedAtDesc(
                        applicationId,
                        branchId
                )
                .stream()
                .map(
                        requestRecord ->
                                mapRequestWithUploadedDocument(
                                        requestRecord,
                                        applicationId,
                                        branchId
                                )
                )
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ApplicationDocumentRequestResponseDTO
    getDocumentRequest(
            CurrentUserContext context,
            Long applicationId,
            Long requestId
    ) {
        Integer branchId =
                requireBranchId(context);

        ErpApplicationDocumentRequest requestRecord =
                requestRepository
                        .findByRequestIdAndApplication_ApplicationIdAndApplication_Branch_BranchIdAndActiveTrue(
                                requirePositiveId(
                                        requestId,
                                        "Document request ID"
                                ),
                                requirePositiveId(
                                        applicationId,
                                        "Application ID"
                                ),
                                branchId
                        )
                        .orElseThrow(
                                () -> new ResourceNotFoundException(
                                        "Application document request was not found."
                                )
                        );

        return mapRequestWithUploadedDocument(
                requestRecord,
                applicationId,
                branchId
        );
    }

    @Override
    @Transactional
    public ApplicationDocumentRequestResponseDTO
    cancelDocumentRequest(
            CurrentUserContext context,
            Long applicationId,
            Long requestId,
            ApplicationDocumentRequestCancelDTO request
    ) {
        requireRequest(
                request,
                "Document-request cancellation details are required."
        );

        String cancellationReason =
                requireText(
                        request.getCancellationReason(),
                        "Cancellation reason"
                );

        Integer branchId =
                requireBranchId(context);

        Integer userId =
                requireUserId(context);

        ErpApplicationDocumentRequest requestRecord =
                requestRepository
                        .findForUpdate(
                                requirePositiveId(
                                        requestId,
                                        "Document request ID"
                                ),
                                requirePositiveId(
                                        applicationId,
                                        "Application ID"
                                ),
                                branchId
                        )
                        .orElseThrow(
                                () -> new ResourceNotFoundException(
                                        "Application document request was not found."
                                )
                        );

        ErpApplication application =
                requestRecord.getApplication();

        ensureWorkflowEditable(application);

        if (requestRecord.getRequestStatus()
                != ErpApplicationDocumentRequest
                .RequestStatus.PENDING) {
            throw new BadRequestException(
                    "Only a pending document request can be cancelled."
            );
        }

        ErpApplication.DocumentStatus previousStatus =
                application.getDocumentStatus();

        LocalDateTime now =
                LocalDateTime.now();

        requestRecord.setRequestStatus(
                ErpApplicationDocumentRequest
                        .RequestStatus.CANCELLED
        );
        requestRecord.setCancelledAt(now);
        requestRecord.setCancelledByUserId(userId);
        requestRecord.setCancellationReason(
                cancellationReason
        );
        requestRecord.setUploadTokenHash(null);
        requestRecord.setUploadTokenExpiresAt(null);
        requestRecord.setEmailRequired(false);
        requestRecord.setEmailStatus(
                ErpApplicationDocumentRequest
                        .EmailStatus.NOT_REQUIRED
        );
        requestRecord.setUpdatedBy(
                userId.longValue()
        );

        ErpApplicationDocumentRequest savedRequest =
                requestRepository.save(
                        requestRecord
                );

        ErpApplication.DocumentStatus recalculatedStatus =
                recalculateApplicationDocumentStatus(
                        application,
                        branchId,
                        userId
                );

        saveHistory(
                application,
                previousStatus,
                recalculatedStatus,
                userId,
                null,
                "Additional-document request cancelled. Request ID: "
                        + savedRequest.getRequestId()
                        + ". Reason: "
                        + cancellationReason,
                false,
                null
        );

        return mapRequestWithUploadedDocument(
                savedRequest,
                applicationId,
                branchId
        );
    }

    private void verifyDocument(
            ErpApplicationDocument document,
            Integer userId,
            LocalDateTime now,
            ApplicationDocumentReviewRequestDTO request
    ) {
        document.setVerificationStatus(
                ErpApplicationDocument
                        .VerificationStatus.VERIFIED
        );
        document.setVerifiedByUserId(userId);
        document.setVerifiedAt(now);

        document.setRejectedByUserId(null);
        document.setRejectedAt(null);
        document.setRejectionReason(null);

        document.setReuploadRequestedAt(null);
        document.setReuploadDeadline(null);

        document.setPublicRemarks(
                trimToNull(request.getPublicRemarks())
        );
        document.setInternalRemarks(
                trimToNull(request.getInternalRemarks())
        );

        ErpApplicationDocumentRequest linkedRequest =
                document.getDocumentRequest();

        if (linkedRequest != null
                && linkedRequest.getRequestStatus()
                == ErpApplicationDocumentRequest
                .RequestStatus.UPLOADED) {
            linkedRequest.setRequestStatus(
                    ErpApplicationDocumentRequest
                            .RequestStatus.COMPLETED
            );
            linkedRequest.setCompletedAt(now);
            linkedRequest.setCompletedByUserId(userId);
            linkedRequest.setUpdatedBy(
                    userId.longValue()
            );
            requestRepository.save(
                    linkedRequest
            );
        }
    }

    private void rejectDocument(
            ErpApplicationDocument document,
            Integer userId,
            LocalDateTime now,
            ApplicationDocumentReviewRequestDTO request
    ) {
        String rejectionReason =
                requireText(
                        request.getRejectionReason(),
                        "Document rejection reason"
                );

        document.setVerificationStatus(
                ErpApplicationDocument
                        .VerificationStatus.REJECTED
        );
        document.setRejectedByUserId(userId);
        document.setRejectedAt(now);
        document.setRejectionReason(
                rejectionReason
        );

        document.setVerifiedByUserId(null);
        document.setVerifiedAt(null);

        document.setReuploadRequestedAt(null);
        document.setReuploadDeadline(null);

        document.setPublicRemarks(
                trimToNull(request.getPublicRemarks())
        );
        document.setInternalRemarks(
                trimToNull(request.getInternalRemarks())
        );
    }

    private void requestDocumentReupload(
            ErpApplication application,
            ErpApplicationDocument document,
            Integer userId,
            LocalDateTime now,
            ApplicationDocumentReviewRequestDTO request
    ) {
        String reuploadReason =
                requireText(
                        request.getReuploadReason(),
                        "Re-upload request reason"
                );

        validateFutureDeadline(
                request.getReuploadDeadline()
        );

        document.setVerificationStatus(
                ErpApplicationDocument
                        .VerificationStatus.REUPLOAD_REQUIRED
        );
        document.setReuploadRequestedAt(now);
        document.setReuploadDeadline(
                request.getReuploadDeadline()
        );
        document.setPublicRemarks(
                firstNonBlank(
                        request.getPublicRemarks(),
                        reuploadReason
                )
        );
        document.setInternalRemarks(
                trimToNull(request.getInternalRemarks())
        );

        document.setVerifiedByUserId(null);
        document.setVerifiedAt(null);
        document.setRejectedByUserId(null);
        document.setRejectedAt(null);
        document.setRejectionReason(null);

        String documentType =
                document.getDocumentType() == null
                        ? ErpApplicationDocument
                        .DocumentType.OTHER.name()
                        : document.getDocumentType().name();

        preventDuplicatePendingRequest(
                application.getApplicationId(),
                application.getBranch().getBranchId(),
                documentType
        );

        GeneratedToken generatedToken =
                generateUniqueUploadToken();

        String requestedName =
                "Replacement "
                        + safeDownloadFileName(
                        document.getOriginalFileName(),
                        document.getDocumentId()
                );

        ErpApplicationDocumentRequest requestRecord =
                buildDocumentRequest(
                        application,
                        documentType,
                        requestedName,
                        reuploadReason,
                        document.getPublicRemarks(),
                        document.getInternalRemarks(),
                        request.getReuploadDeadline(),
                        userId,
                        now,
                        generatedToken.hash()
                );

        ErpApplicationDocumentRequest savedRequest =
                requestRepository.save(
                        requestRecord
                );

        publishAdditionalDocumentEmail(
                savedRequest,
                generatedToken.raw()
        );
    }

    private void publishAdditionalDocumentEmail(
            ErpApplicationDocumentRequest requestRecord,
            String rawUploadToken
    ) {
        if (requestRecord == null
                || requestRecord.getRequestId() == null
                || requestRecord.getRequestId() <= 0) {
            throw new IllegalStateException(
                    "Saved document request ID is required for email delivery."
            );
        }

        if (!StringUtils.hasText(rawUploadToken)) {
            throw new IllegalStateException(
                    "Raw document upload token is required for email delivery."
            );
        }

        applicationEventPublisher.publishEvent(
                new AdditionalDocumentEmailRequestedEvent(
                        requestRecord.getRequestId(),
                        rawUploadToken
                )
        );
    }

    private String resolveRequestedDocumentName(
            String requestedDocumentName,
            String documentType
    ) {
        String providedName =
                trimToNull(
                        requestedDocumentName
                );

        if (providedName != null) {
            return providedName;
        }

        return toDisplayLabel(
                documentType
        );
    }

    private String resolveRequestReason(
            String requestReason
    ) {
        String providedReason =
                trimToNull(
                        requestReason
                );

        return providedReason != null
                ? providedReason
                : "Required for application verification.";
    }

    private String toDisplayLabel(
            String value
    ) {
        String normalized =
                requireText(
                        value,
                        "Requested document type"
                )
                        .trim()
                        .toLowerCase(Locale.ROOT)
                        .replace('_', ' ')
                        .replace('-', ' ');

        StringBuilder label =
                new StringBuilder(
                        normalized.length()
                );

        boolean capitalizeNext = true;

        for (int index = 0;
             index < normalized.length();
             index++) {
            char character =
                    normalized.charAt(index);

            if (Character.isWhitespace(character)) {
                if (!label.isEmpty()
                        && label.charAt(
                        label.length() - 1
                ) != ' ') {
                    label.append(' ');
                }

                capitalizeNext = true;
                continue;
            }

            label.append(
                    capitalizeNext
                            ? Character.toUpperCase(character)
                            : character
            );

            capitalizeNext = false;
        }

        return label.toString();
    }

    private ErpApplicationDocumentRequest
    buildDocumentRequest(
            ErpApplication application,
            String documentType,
            String documentName,
            String requestReason,
            String publicRemarks,
            String internalRemarks,
            LocalDateTime uploadDeadline,
            Integer userId,
            LocalDateTime now,
            String tokenHash
    ) {
        ErpApplicationDocumentRequest requestRecord =
                new ErpApplicationDocumentRequest();

        requestRecord.setApplication(application);
        requestRecord.setRequestedDocumentType(
                documentType
        );
        requestRecord.setRequestedDocumentName(
                documentName
        );
        requestRecord.setRequestReason(
                requestReason
        );
        requestRecord.setPublicRemarks(
                publicRemarks
        );
        requestRecord.setInternalRemarks(
                internalRemarks
        );
        requestRecord.setRequestStatus(
                ErpApplicationDocumentRequest
                        .RequestStatus.PENDING
        );
        requestRecord.setRequestedByUserId(userId);
        requestRecord.setRequestedAt(now);
        requestRecord.setUploadDeadline(
                uploadDeadline
        );
        requestRecord.setUploadTokenHash(
                tokenHash
        );
        requestRecord.setUploadTokenExpiresAt(
                calculateTokenExpiry(
                        now,
                        uploadDeadline
                )
        );
        requestRecord.setEmailRequired(true);
        requestRecord.setEmailStatus(
                ErpApplicationDocumentRequest
                        .EmailStatus.PENDING
        );
        requestRecord.setActive(true);
        requestRecord.setCreatedBy(
                userId.longValue()
        );
        requestRecord.setUpdatedBy(
                userId.longValue()
        );

        return requestRecord;
    }

    private ErpApplication.DocumentStatus
    recalculateApplicationDocumentStatus(
            ErpApplication application,
            Integer branchId,
            Integer userId
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
        application.setUpdatedBy(
                userId.longValue()
        );

        applicationRepository.save(
                application
        );

        return status;
    }

    private void saveHistory(
            ErpApplication application,
            Enum<?> oldStatus,
            Enum<?> newStatus,
            Integer userId,
            String publicRemarks,
            String internalRemarks,
            boolean emailRequired,
            String emailType
    ) {
        ErpApplicationStatusHistory history =
                new ErpApplicationStatusHistory();

        history.setApplication(application);
        history.setStage(DOCUMENT_STAGE);
        history.setOldStatus(oldStatus);
        history.setNewStatus(newStatus);
        history.setChangedBy(
                userId == null
                        ? null
                        : userId.longValue()
        );
        history.setPublicRemarks(
                trimToNull(publicRemarks)
        );
        history.setInternalRemarks(
                trimToNull(internalRemarks)
        );
        history.setTransitionSource(
                TRANSITION_SOURCE
        );
        history.setEmailRequired(
                emailRequired
        );
        history.setEmailStatus(
                emailRequired
                        ? EMAIL_PENDING
                        : ErpApplicationStatusHistory
                        .EMAIL_NOT_REQUIRED
        );
        history.setEmailType(
                emailType
        );

        historyRepository.save(
                history
        );
    }

    private ApplicationDocumentRequestResponseDTO
    mapRequestWithUploadedDocument(
            ErpApplicationDocumentRequest requestRecord,
            Long applicationId,
            Integer branchId
    ) {
        ErpApplicationDocument uploadedDocument =
                documentRepository
                        .findByDocumentRequest_RequestIdAndApplication_ApplicationIdAndApplication_Branch_BranchIdAndActiveTrue(
                                requestRecord.getRequestId(),
                                applicationId,
                                branchId
                        )
                        .orElse(null);

        return documentMapper
                .toDocumentRequestResponse(
                        requestRecord,
                        uploadedDocument
                );
    }

    /**
     * Repairs records that were rolled back by the older document workflow.
     * A persisted School Visit schedule proves the application had already
     * progressed beyond APPLICATION_VERIFICATION, so document operations must
     * not force the user through School Visit scheduling a second time.
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

    private ErpApplication requireAccessibleApplication(
            Long applicationId,
            Integer branchId
    ) {
        Long validatedApplicationId =
                requirePositiveId(
                        applicationId,
                        "Application ID"
                );

        ErpApplication application =
                applicationRepository
                        .findById(
                                validatedApplicationId
                        )
                        .orElseThrow(
                                () -> new ResourceNotFoundException(
                                        "Application was not found."
                                )
                        );

        if (application.getBranch() == null
                || application.getBranch().getBranchId() == null
                || !branchId.equals(
                application.getBranch().getBranchId()
        )) {
            throw new ResourceNotFoundException(
                    "Application was not found."
            );
        }

        if (application.getStatus() != null
                && application.getStatus() != 1) {
            throw new ResourceNotFoundException(
                    "Application was not found."
            );
        }

        return application;
    }

    private ErpApplicationDocument findAccessibleDocument(
            Long applicationId,
            Long documentId,
            Integer branchId
    ) {
        return documentRepository
                .findByDocumentIdAndApplication_ApplicationIdAndApplication_Branch_BranchIdAndActiveTrue(
                        requirePositiveId(
                                documentId,
                                "Document ID"
                        ),
                        requirePositiveId(
                                applicationId,
                                "Application ID"
                        ),
                        branchId
                )
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Application document was not found."
                        )
                );
    }

    private Integer requireBranchId(
            CurrentUserContext context
    ) {
        return branchAccessService
                .getValidatedBranchId(
                        context
                );
    }

    private Integer requireUserId(
            CurrentUserContext context
    ) {
        if (context == null
                || context.getUserId() == null) {
            throw new BadRequestException(
                    "Authenticated user ID is unavailable."
            );
        }

        return context.getUserId();
    }

    private void ensureWorkflowEditable(
            ErpApplication application
    ) {
        if (Boolean.TRUE.equals(
                application.getWorkflowLocked()
        )) {
            throw new BadRequestException(
                    "The completed admission workflow is locked."
            );
        }

        if (application.getAdmissionStatus()
                == ErpApplication.AdmissionStatus.ENROLLED
                || application.getCurrentStage()
                == ErpApplication.CurrentStage.ENROLLED) {
            throw new BadRequestException(
                    "Documents cannot be changed after enrollment."
            );
        }
    }

    private void preventDuplicatePendingRequest(
            Long applicationId,
            Integer branchId,
            String documentType
    ) {
        boolean duplicateExists =
                requestRepository
                        .findAllByApplication_ApplicationIdAndApplication_Branch_BranchIdAndRequestStatusAndActiveTrueOrderByRequestedAtAsc(
                                applicationId,
                                branchId,
                                ErpApplicationDocumentRequest
                                        .RequestStatus.PENDING
                        )
                        .stream()
                        .anyMatch(
                                existing ->
                                        documentType.equalsIgnoreCase(
                                                existing
                                                        .getRequestedDocumentType()
                                        )
                        );

        if (duplicateExists) {
            throw new BadRequestException(
                    "A pending request already exists for this document type."
            );
        }
    }

    private GeneratedToken generateUniqueUploadToken() {
        for (int attempt = 0;
             attempt < TOKEN_CREATION_ATTEMPTS;
             attempt++) {

            byte[] tokenBytes =
                    new byte[TOKEN_BYTE_LENGTH];

            secureRandom.nextBytes(
                    tokenBytes
            );

            String rawToken =
                    Base64.getUrlEncoder()
                            .withoutPadding()
                            .encodeToString(
                                    tokenBytes
                            );

            String tokenHash =
                    sha256Hex(
                            rawToken
                    );

            if (!requestRepository
                    .existsByUploadTokenHash(
                            tokenHash
                    )) {
                return new GeneratedToken(
                        rawToken,
                        tokenHash
                );
            }
        }

        throw new IllegalStateException(
                "Could not generate a unique document upload token."
        );
    }

    private String sha256Hex(
            String value
    ) {
        try {
            MessageDigest digest =
                    MessageDigest.getInstance(
                            "SHA-256"
                    );

            return HexFormat.of()
                    .formatHex(
                            digest.digest(
                                    value.getBytes(
                                            StandardCharsets.UTF_8
                                    )
                            )
                    );
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "SHA-256 is not available.",
                    exception
            );
        }
    }

    private LocalDateTime calculateTokenExpiry(
            LocalDateTime now,
            LocalDateTime uploadDeadline
    ) {
        LocalDateTime configuredExpiry =
                now.plusHours(
                        uploadTokenValidityHours
                );

        if (uploadDeadline != null
                && uploadDeadline.isBefore(
                configuredExpiry
        )) {
            return uploadDeadline;
        }

        return configuredExpiry;
    }

    private Path resolveStoredFile(
            String storedPath
    ) {
        if (!StringUtils.hasText(storedPath)) {
            throw new ResourceNotFoundException(
                    "Application document path is unavailable."
            );
        }

        String normalizedStoredPath =
                storedPath.trim()
                        .replace('\\', '/');

        while (normalizedStoredPath.startsWith("/")) {
            normalizedStoredPath =
                    normalizedStoredPath.substring(1);
        }

        if (!StringUtils.hasText(
                normalizedStoredPath
        )) {
            throw new ResourceNotFoundException(
                    "Application document path is invalid."
            );
        }

        Path relativePath;

        try {
            relativePath =
                    Path.of(
                            normalizedStoredPath
                    ).normalize();
        } catch (RuntimeException exception) {
            throw new ResourceNotFoundException(
                    "Application document path is invalid."
            );
        }

        if (relativePath.isAbsolute()
                || relativePath.startsWith("..")) {
            throw new ResourceNotFoundException(
                    "Application document path is invalid."
            );
        }

        Path rootName =
                publicUploadRoot.getFileName();

        if (rootName != null
                && relativePath.getNameCount() > 0
                && rootName.toString().equalsIgnoreCase(
                relativePath.getName(0).toString()
        )) {
            relativePath =
                    relativePath.getNameCount() == 1
                            ? Path.of("")
                            : relativePath.subpath(
                            1,
                            relativePath.getNameCount()
                    );
        }

        Path candidate =
                publicUploadRoot
                        .resolve(relativePath)
                        .normalize();

        if (!candidate.startsWith(
                publicUploadRoot
        )) {
            throw new ResourceNotFoundException(
                    "Application document path is invalid."
            );
        }

        try {
            if (!Files.exists(
                    publicUploadRoot,
                    LinkOption.NOFOLLOW_LINKS
            )) {
                throw new ResourceNotFoundException(
                        "Application document storage is unavailable."
                );
            }

            if (!Files.isRegularFile(
                    candidate,
                    LinkOption.NOFOLLOW_LINKS
            )
                    || Files.isSymbolicLink(candidate)) {
                throw new ResourceNotFoundException(
                        "Application document file was not found."
                );
            }

            Path realRoot =
                    publicUploadRoot.toRealPath();

            Path realCandidate =
                    candidate.toRealPath();

            if (!realCandidate.startsWith(
                    realRoot
            )) {
                throw new ResourceNotFoundException(
                        "Application document path is invalid."
                );
            }

            return realCandidate;
        } catch (IOException exception) {
            throw new ResourceNotFoundException(
                    "Application document file was not found."
            );
        }
    }

    private String detectContentType(
            Path filePath,
            String storedContentType
    ) {
        try {
            String detected =
                    Files.probeContentType(
                            filePath
                    );

            if (StringUtils.hasText(detected)) {
                return detected
                        .trim()
                        .toLowerCase(Locale.ROOT);
            }
        } catch (IOException ignored) {
            // Safe fallback below.
        }

        if (StringUtils.hasText(
                storedContentType
        )) {
            String normalized =
                    storedContentType
                            .trim()
                            .toLowerCase(Locale.ROOT);

            if (normalized.matches(
                    "[a-z0-9.+-]+/[a-z0-9.+-]+"
            )) {
                return normalized;
            }
        }

        return "application/octet-stream";
    }

    private String safeDownloadFileName(
            String originalFileName,
            Long documentId
    ) {
        String fallback =
                "application-document-"
                        + (documentId == null
                        ? "file"
                        : documentId);

        if (!StringUtils.hasText(
                originalFileName
        )) {
            return fallback;
        }

        String safeName =
                originalFileName
                        .replace('\\', '/');

        int lastSlash =
                safeName.lastIndexOf('/');

        if (lastSlash >= 0) {
            safeName =
                    safeName.substring(
                            lastSlash + 1
                    );
        }

        safeName =
                safeName.replaceAll(
                        "[\\r\\n\\t\\u0000-\\u001F\\u007F]",
                        ""
                ).trim();

        return StringUtils.hasText(safeName)
                ? safeName
                : fallback;
    }

    private String normalizeDocumentType(
            String value
    ) {
        String normalized =
                requireText(
                        value,
                        "Requested document type"
                )
                        .toUpperCase(Locale.ROOT)
                        .replaceAll(
                                "[^A-Z0-9]+",
                                "_"
                        )
                        .replaceAll(
                                "^_+|_+$",
                                ""
                        );

        if (!StringUtils.hasText(normalized)
                || normalized.length() > 50) {
            throw new BadRequestException(
                    "Requested document type is invalid."
            );
        }

        return normalized;
    }

    private void validateFutureDeadline(
            LocalDateTime deadline
    ) {
        if (deadline != null
                && !deadline.isAfter(
                LocalDateTime.now()
        )) {
            throw new BadRequestException(
                    "Upload deadline must be in the future."
            );
        }
    }

    private String requireText(
            String value,
            String fieldName
    ) {
        if (!StringUtils.hasText(value)) {
            throw new BadRequestException(
                    fieldName + " is required."
            );
        }

        return value.trim();
    }

    private Long requirePositiveId(
            Long id,
            String fieldName
    ) {
        if (id == null || id <= 0) {
            throw new BadRequestException(
                    fieldName + " is invalid."
            );
        }

        return id;
    }

    private void requireRequest(
            Object request,
            String message
    ) {
        if (request == null) {
            throw new BadRequestException(
                    message
            );
        }
    }

    private String trimToNull(
            String value
    ) {
        return StringUtils.hasText(value)
                ? value.trim()
                : null;
    }

    private String firstNonBlank(
            String preferred,
            String fallback
    ) {
        return StringUtils.hasText(preferred)
                ? preferred.trim()
                : trimToNull(fallback);
    }

    /**
     * Clears the applicant-photo pointer only when it references the exact
     * document file being deactivated. Deleting another duplicate PHOTO row
     * therefore does not remove the current applicant photo.
     */
    private void clearApplicationPhotoReferenceWhenDeleted(
            ErpApplication application,
            ErpApplicationDocument document
    ) {
        if (application == null
                || document == null
                || document.getDocumentType()
                != ErpApplicationDocument.DocumentType.PHOTO
                || !sameStoredPublicPath(
                application.getPhotoPath(),
                document.getFilePath()
        )) {
            return;
        }

        application.setPhotoPath("");
    }

    /**
     * When a current document is removed and the remaining active documents
     * are no longer fully verified, the workflow returns to application
     * verification rather than continuing with an invalid approved state.
     */
    private void synchronizeWorkflowAfterDocumentDeletion(
            ErpApplication application,
            boolean deletedDocumentWasCurrent,
            ErpApplication.DocumentStatus recalculatedStatus,
            Integer userId
    ) {
        if (!deletedDocumentWasCurrent
                || recalculatedStatus
                == ErpApplication.DocumentStatus.VERIFIED) {
            return;
        }

        /*
         * Removing a current document can make document verification
         * incomplete, but it must not rewind an application that already
         * reached School Visit or a later workflow stage. The document state
         * will block progression until resolved.
         */
        if (recalculatedStatus
                != ErpApplication.DocumentStatus.REUPLOAD_REQUIRED) {
            application.setVerificationStatus(
                    ErpApplication.VerificationStatus.PENDING
            );
        }

        application.setUpdatedBy(
                userId.longValue()
        );

        applicationRepository.save(
                application
        );
    }

    private String buildDocumentDeletionInternalRemarks(
            ErpApplicationDocument document,
            ApplicationDocumentDeleteRequestDTO.DeletionReason reason,
            String deletionDetails
    ) {
        StringBuilder remarks =
                new StringBuilder();

        remarks.append("Document ID: ")
                .append(document.getDocumentId())
                .append(". File: ")
                .append(
                        safeDownloadFileName(
                                document.getOriginalFileName(),
                                document.getDocumentId()
                        )
                )
                .append(". Deletion reason: ")
                .append(reason.name())
                .append(". Details: ")
                .append(deletionDetails.trim())
                .append(
                        ". Physical public-upload file deletion "
                                + "requested after transaction commit."
                );

        return limitAuditText(
                remarks.toString()
        );
    }

    private String appendAuditRemark(
            String existingRemarks,
            String newAuditRemark
    ) {
        String existing =
                trimToNull(existingRemarks);

        String addition =
                trimToNull(newAuditRemark);

        if (existing == null) {
            return limitAuditText(addition);
        }

        if (addition == null) {
            return limitAuditText(existing);
        }

        return limitAuditText(
                existing + " " + addition
        );
    }

    /**
     * Both document.internal_remarks and history.internal_remarks are limited
     * to 1000 characters. The newest audit information is retained when an
     * older remark makes the combined value exceed that limit.
     */
    private String limitAuditText(
            String value
    ) {
        String normalized =
                trimToNull(value);

        if (normalized == null
                || normalized.length() <= 1000) {
            return normalized;
        }

        return normalized.substring(
                normalized.length() - 1000
        );
    }

    private boolean sameStoredPublicPath(
            String firstPath,
            String secondPath
    ) {
        String first =
                normalizeStoredPublicPathKey(
                        firstPath
                );

        String second =
                normalizeStoredPublicPathKey(
                        secondPath
                );

        return first != null
                && first.equals(second);
    }

    private String normalizeStoredPublicPathKey(
            String storedPath
    ) {
        if (!StringUtils.hasText(storedPath)) {
            return null;
        }

        String normalized =
                storedPath.trim()
                        .replace('\\', '/');

        while (normalized.startsWith("/")) {
            normalized =
                    normalized.substring(1);
        }

        if (normalized.regionMatches(
                true,
                0,
                "uploads/",
                0,
                "uploads/".length()
        )) {
            normalized =
                    normalized.substring(
                            "uploads/".length()
                    );
        }

        return StringUtils.hasText(normalized)
                ? normalized
                : null;
    }

    private String buildReviewInternalRemarks(
            ErpApplicationDocument document,
            ApplicationDocumentReviewRequestDTO request
    ) {
        StringBuilder remarks =
                new StringBuilder();

        remarks.append("Document ID: ")
                .append(document.getDocumentId())
                .append(". Decision: ")
                .append(request.getDecision().name())
                .append('.');

        if (StringUtils.hasText(
                request.getInternalRemarks()
        )) {
            remarks.append(" ")
                    .append(
                            request.getInternalRemarks()
                                    .trim()
                    );
        }

        if (StringUtils.hasText(
                request.getRejectionReason()
        )) {
            remarks.append(" Rejection reason: ")
                    .append(
                            request.getRejectionReason()
                                    .trim()
                    );
        }

        if (StringUtils.hasText(
                request.getReuploadReason()
        )) {
            remarks.append(" Re-upload reason: ")
                    .append(
                            request.getReuploadReason()
                                    .trim()
                    );
        }

        return remarks.toString();
    }

    private record GeneratedToken(
            String raw,
            String hash
    ) {
    }
}
