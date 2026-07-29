package com.erp.montfortuganda.student.service;

import com.erp.montfortuganda.exception.BadRequestException;
import com.erp.montfortuganda.exception.ResourceNotFoundException;
import com.erp.montfortuganda.school.entity.Branch;
import com.erp.montfortuganda.student.dto.request.StudentDocumentUploadRequest;
import com.erp.montfortuganda.student.dto.request.StudentDocumentVerificationRequest;
import com.erp.montfortuganda.student.dto.response.StudentDocumentResponse;
import com.erp.montfortuganda.student.entity.ErpStudent;
import com.erp.montfortuganda.student.entity.ErpStudentDocument;
import com.erp.montfortuganda.student.mapper.StudentMapper;
import com.erp.montfortuganda.student.repository.ErpStudentDocumentRepository;
import jakarta.persistence.EntityManager;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Handles Student document upload, listing, verification and protected
 * file retrieval.
 *
 * <p>All document operations are restricted to the authenticated user's
 * assigned branch.</p>
 */
@Service
public class StudentDocumentService {

    private final ErpStudentDocumentRepository documentRepository;
    private final StudentValidationService validationService;
    private final StudentFileService fileService;
    private final StudentMapper studentMapper;
    private final EntityManager entityManager;

    public StudentDocumentService(
            ErpStudentDocumentRepository documentRepository,
            StudentValidationService validationService,
            StudentFileService fileService,
            StudentMapper studentMapper,
            EntityManager entityManager
    ) {
        this.documentRepository = documentRepository;
        this.validationService = validationService;
        this.fileService = fileService;
        this.studentMapper = studentMapper;
        this.entityManager = entityManager;
    }

    // =====================================================================
    // UPLOAD
    // =====================================================================

    /**
     * Stores a new private Student document and creates its database record.
     *
     * <p>If the transaction rolls back, StudentFileService removes the newly
     * stored physical file automatically.</p>
     */
    @Transactional
    public StudentDocumentResponse uploadDocument(
            Long studentId,
            StudentDocumentUploadRequest request
    ) {
        validateUploadRequest(request);

        StudentValidationService.BranchContext branchContext =
                validationService.requireAuthenticatedBranch();

        Branch branch =
                branchContext.branch();

        ErpStudent student =
                validationService.requireStudent(
                        studentId,
                        branch.getBranchId()
                );

        Long authenticatedUserId =
                toLongUserId(
                        branchContext.userId()
                );

        String filePrefix =
                request.metadata()
                        .documentType()
                        .name()
                        .toLowerCase(Locale.ROOT);

        StudentFileService.StoredFile storedFile =
                fileService.storeStudentDocument(
                        request.file(),
                        student,
                        filePrefix
                );

        ErpStudentDocument document =
                studentMapper.toNewDocument(
                        request.metadata(),
                        student,
                        branch,
                        storedFile.storedFileName(),
                        storedFile.originalFileName(),
                        storedFile.relativePath(),
                        storedFile.extension(),
                        storedFile.mimeType(),
                        storedFile.size(),
                        authenticatedUserId
                );

        ErpStudentDocument savedDocument =
                documentRepository.saveAndFlush(
                        document
                );

        /*
         * uploaded_at and created_at may be populated by database defaults.
         * Refresh ensures the response contains those generated values.
         */
        entityManager.refresh(
                savedDocument
        );

        return studentMapper.toDocumentResponse(
                savedDocument,
                branchContext.username(),
                null
        );
    }

    // =====================================================================
    // LIST AND VIEW
    // =====================================================================

    @Transactional(readOnly = true)
    public List<StudentDocumentResponse> getStudentDocuments(
            Long studentId
    ) {
        StudentValidationService.BranchContext branchContext =
                validationService.requireAuthenticatedBranch();

        Integer branchId =
                branchContext.branch()
                        .getBranchId();

        validationService.requireStudent(
                studentId,
                branchId
        );

        return findActiveDocuments(
                studentId,
                branchId
        )
                .stream()
                .map(document ->
                        studentMapper.toDocumentResponse(
                                document,
                                null,
                                null
                        )
                )
                .toList();
    }

    @Transactional(readOnly = true)
    public StudentDocumentResponse getDocument(
            Long studentId,
            Long documentId
    ) {
        StudentValidationService.BranchContext branchContext =
                validationService.requireAuthenticatedBranch();

        ErpStudentDocument document =
                requireDocument(
                        studentId,
                        documentId,
                        branchContext.branch()
                                .getBranchId()
                );

        return studentMapper.toDocumentResponse(
                document,
                null,
                null
        );
    }

    /**
     * Used by the main Student profile service after branch ownership has
     * already been validated.
     */
    @Transactional(readOnly = true)
    public List<StudentDocumentResponse> getDocumentsForProfile(
            Long studentId,
            Integer branchId
    ) {
        if (studentId == null || studentId <= 0) {
            throw new BadRequestException(
                    "A valid Student ID is required."
            );
        }

        if (branchId == null || branchId <= 0) {
            throw new BadRequestException(
                    "A valid branch ID is required."
            );
        }

        return findActiveDocuments(
                studentId,
                branchId
        )
                .stream()
                .map(document ->
                        studentMapper.toDocumentResponse(
                                document,
                                null,
                                null
                        )
                )
                .toList();
    }

    // =====================================================================
    // VERIFICATION
    // =====================================================================

    @Transactional
    public StudentDocumentResponse verifyDocument(
            Long studentId,
            Long documentId,
            StudentDocumentVerificationRequest request
    ) {
        validateVerificationRequest(
                request
        );

        StudentValidationService.BranchContext branchContext =
                validationService.requireAuthenticatedBranch();

        ErpStudentDocument document =
                requireDocument(
                        studentId,
                        documentId,
                        branchContext.branch()
                                .getBranchId()
                );

        if (
                document.getDocumentStatus()
                        == request.documentStatus()
        ) {
            throw new BadRequestException(
                    "Student document is already in status "
                            + request.documentStatus().name()
                            + "."
            );
        }

        studentMapper.applyDocumentVerification(
                request,
                document,
                toLongUserId(branchContext.userId())
        );

        ErpStudentDocument savedDocument =
                documentRepository.saveAndFlush(
                        document
                );

        return studentMapper.toDocumentResponse(
                savedDocument,
                null,
                branchContext.username()
        );
    }

    // =====================================================================
    // PROTECTED DOWNLOAD
    // =====================================================================

    @Transactional(readOnly = true)
    public StudentDocumentFile loadDocumentFile(
            Long studentId,
            Long documentId
    ) {
        StudentValidationService.BranchContext branchContext =
                validationService.requireAuthenticatedBranch();

        ErpStudentDocument document =
                requireDocument(
                        studentId,
                        documentId,
                        branchContext.branch()
                                .getBranchId()
                );

        Resource resource =
                fileService.loadPrivateFile(
                        document.getFilePath()
                );

        String contentType =
                fileService.detectContentType(
                        document.getFilePath()
                );

        long fileSize =
                fileService.getStoredFileSize(
                        document.getFilePath()
                );

        String downloadName =
                resolveDownloadName(
                        document
                );

        return new StudentDocumentFile(
                resource,
                downloadName,
                contentType,
                fileSize
        );
    }

    // =====================================================================
    // DEACTIVATE
    // =====================================================================

    /**
     * Soft-deactivates the database document and deletes the physical file
     * only after the database transaction commits successfully.
     */
    @Transactional
    public void deactivateDocument(
            Long studentId,
            Long documentId
    ) {
        StudentValidationService.BranchContext branchContext =
                validationService.requireAuthenticatedBranch();

        ErpStudentDocument document =
                requireDocument(
                        studentId,
                        documentId,
                        branchContext.branch()
                                .getBranchId()
                );

        document.setActive(false);

        documentRepository.saveAndFlush(
                document
        );

        fileService.scheduleDeleteAfterCommit(
                document.getFilePath()
        );
    }

    // =====================================================================
    // DATABASE LOOKUPS
    // =====================================================================

    private List<ErpStudentDocument> findActiveDocuments(
            Long studentId,
            Integer branchId
    ) {
        return entityManager
                .createQuery(
                        """
                        select document
                        from ErpStudentDocument document
                        where document.student.studentId = :studentId
                          and document.branch.branchId = :branchId
                          and document.active = true
                        order by document.uploadedAt desc,
                                 document.documentId desc
                        """,
                        ErpStudentDocument.class
                )
                .setParameter(
                        "studentId",
                        studentId
                )
                .setParameter(
                        "branchId",
                        branchId
                )
                .getResultList();
    }

    private ErpStudentDocument requireDocument(
            Long studentId,
            Long documentId,
            Integer branchId
    ) {
        if (studentId == null || studentId <= 0) {
            throw new BadRequestException(
                    "A valid Student ID is required."
            );
        }

        if (documentId == null || documentId <= 0) {
            throw new BadRequestException(
                    "A valid Student document ID is required."
            );
        }

        return entityManager
                .createQuery(
                        """
                        select document
                        from ErpStudentDocument document
                        where document.documentId = :documentId
                          and document.student.studentId = :studentId
                          and document.branch.branchId = :branchId
                          and document.active = true
                        """,
                        ErpStudentDocument.class
                )
                .setParameter(
                        "documentId",
                        documentId
                )
                .setParameter(
                        "studentId",
                        studentId
                )
                .setParameter(
                        "branchId",
                        branchId
                )
                .getResultStream()
                .findFirst()
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Student document was not found."
                        )
                );
    }

    // =====================================================================
    // REQUEST VALIDATION
    // =====================================================================

    private void validateUploadRequest(
            StudentDocumentUploadRequest request
    ) {
        if (request == null) {
            throw new BadRequestException(
                    "Student document upload request is required."
            );
        }

        if (request.metadata() == null) {
            throw new BadRequestException(
                    "Student document metadata is required."
            );
        }

        if (request.metadata().documentType() == null) {
            throw new BadRequestException(
                    "Student document type is required."
            );
        }

        if (
                !StringUtils.hasText(
                        request.metadata().documentName()
                )
        ) {
            throw new BadRequestException(
                    "Student document name is required."
            );
        }

        if (
                request.file() == null
                        || request.file().isEmpty()
        ) {
            throw new BadRequestException(
                    "Student document file is required."
            );
        }
    }

    private void validateVerificationRequest(
            StudentDocumentVerificationRequest request
    ) {
        if (request == null) {
            throw new BadRequestException(
                    "Student document verification request is required."
            );
        }

        if (request.documentStatus() == null) {
            throw new BadRequestException(
                    "Student document verification status is required."
            );
        }

        boolean supportedStatus =
                request.documentStatus()
                        == ErpStudentDocument.DocumentStatus.VERIFIED
                        || request.documentStatus()
                        == ErpStudentDocument.DocumentStatus.REJECTED;

        if (!supportedStatus) {
            throw new BadRequestException(
                    "Student documents can only be verified or rejected through this operation."
            );
        }

        if (
                request.documentStatus()
                        == ErpStudentDocument.DocumentStatus.REJECTED
                        && !StringUtils.hasText(request.remarks())
        ) {
            throw new BadRequestException(
                    "Rejection remarks are required."
            );
        }
    }

    // =====================================================================
    // HELPERS
    // =====================================================================

    private Long toLongUserId(
            Integer userId
    ) {
        if (userId == null || userId <= 0) {
            throw new BadRequestException(
                    "Authenticated user ID is unavailable."
            );
        }

        return userId.longValue();
    }

    private String resolveDownloadName(
            ErpStudentDocument document
    ) {
        if (
                StringUtils.hasText(
                        document.getOriginalFileName()
                )
        ) {
            return document
                    .getOriginalFileName()
                    .trim();
        }

        if (StringUtils.hasText(document.getFileName())) {
            return document
                    .getFileName()
                    .trim();
        }

        String extension =
                StringUtils.hasText(
                        document.getFileExtension()
                )
                        ? "."
                          + document.getFileExtension()
                        .trim()
                        .toLowerCase(Locale.ROOT)
                        : "";

        return "student-document-"
                + Objects.toString(
                document.getDocumentId(),
                "file"
        )
                + extension;
    }

    // =====================================================================
    // RESULT
    // =====================================================================

    public record StudentDocumentFile(
            Resource resource,
            String downloadName,
            String contentType,
            long fileSize
    ) {
    }
}