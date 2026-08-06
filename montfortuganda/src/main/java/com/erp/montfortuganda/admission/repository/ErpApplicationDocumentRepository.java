package com.erp.montfortuganda.admission.repository;

import com.erp.montfortuganda.admission.entity.ErpApplicationDocument;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Branch-scoped persistence operations for public-application documents.
 *
 * Browser-supplied application and document identifiers must always be
 * resolved together with the authenticated branch ID before file metadata or
 * stored file content is returned.
 */
@Repository
public interface ErpApplicationDocumentRepository
        extends JpaRepository<ErpApplicationDocument, Long> {

    /**
     * Returns the latest active document versions for one application.
     */
    @EntityGraph(attributePaths = {
            "application",
            "application.branch",
            "documentRequest",
            "replacementDocument"
    })
    List<ErpApplicationDocument>
    findAllByApplication_ApplicationIdAndApplication_Branch_BranchIdAndCurrentTrueAndActiveTrueOrderByUploadedAtAsc(
            Long applicationId,
            Integer branchId
    );

    /**
     * Returns all active versions, including superseded records, for audit and
     * replacement-history views.
     */
    @EntityGraph(attributePaths = {
            "application",
            "application.branch",
            "documentRequest",
            "replacementDocument"
    })
    List<ErpApplicationDocument>
    findAllByApplication_ApplicationIdAndApplication_Branch_BranchIdAndActiveTrueOrderByUploadedAtDesc(
            Long applicationId,
            Integer branchId
    );

    /**
     * Branch-safe lookup used before opening or downloading one stored file.
     * Historical superseded files remain accessible to authorized branch users.
     */
    @EntityGraph(attributePaths = {
            "application",
            "application.branch",
            "documentRequest",
            "replacementDocument"
    })
    Optional<ErpApplicationDocument>
    findByDocumentIdAndApplication_ApplicationIdAndApplication_Branch_BranchIdAndActiveTrue(
            Long documentId,
            Long applicationId,
            Integer branchId
    );

    /**
     * Locks the current document while a verification, rejection or re-upload
     * decision is being saved, preventing concurrent conflicting decisions.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select documentRecord
            from ErpApplicationDocument documentRecord
            join fetch documentRecord.application applicationRecord
            join fetch applicationRecord.branch branchRecord
            left join fetch documentRecord.documentRequest documentRequest
            where documentRecord.documentId = :documentId
              and applicationRecord.applicationId = :applicationId
              and branchRecord.branchId = :branchId
              and documentRecord.current = true
              and documentRecord.active = true
            """)
    Optional<ErpApplicationDocument> findCurrentForReview(
            @Param("documentId") Long documentId,
            @Param("applicationId") Long applicationId,
            @Param("branchId") Integer branchId
    );

    /**
     * Resolves the file uploaded for one additional-document request.
     */
    @EntityGraph(attributePaths = {
            "application",
            "application.branch",
            "documentRequest"
    })
    Optional<ErpApplicationDocument>
    findByDocumentRequest_RequestIdAndApplication_ApplicationIdAndApplication_Branch_BranchIdAndActiveTrue(
            Long requestId,
            Long applicationId,
            Integer branchId
    );

    /**
     * Supports branch document-review queues.
     */
    @EntityGraph(attributePaths = {
            "application",
            "application.branch",
            "documentRequest"
    })
    Page<ErpApplicationDocument>
    findAllByApplication_Branch_BranchIdAndVerificationStatusAndCurrentTrueAndActiveTrue(
            Integer branchId,
            ErpApplicationDocument.VerificationStatus verificationStatus,
            Pageable pageable
    );

    /**
     * Returns current documents in one status for application-level status
     * recalculation and validation.
     */
    List<ErpApplicationDocument>
    findAllByApplication_ApplicationIdAndApplication_Branch_BranchIdAndVerificationStatusAndCurrentTrueAndActiveTrueOrderByUploadedAtAsc(
            Long applicationId,
            Integer branchId,
            ErpApplicationDocument.VerificationStatus verificationStatus
    );

    long countByApplication_ApplicationIdAndApplication_Branch_BranchIdAndCurrentTrueAndActiveTrue(
            Long applicationId,
            Integer branchId
    );

    long countByApplication_ApplicationIdAndApplication_Branch_BranchIdAndVerificationStatusAndCurrentTrueAndActiveTrue(
            Long applicationId,
            Integer branchId,
            ErpApplicationDocument.VerificationStatus verificationStatus
    );

    boolean existsByDocumentRequest_RequestIdAndActiveTrue(Long requestId);
}
