package com.erp.montfortuganda.admission.repository;

import com.erp.montfortuganda.admission.entity.ErpApplicationDocumentRequest;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Persistence operations for additional-document requests raised during the
 * admission verification process.
 *
 * Branch users must resolve requests together with the authenticated branch
 * ID. Public upload flows must resolve requests only through the stored token
 * hash and must still validate status and expiry in the service layer.
 */
@Repository
public interface ErpApplicationDocumentRequestRepository
        extends JpaRepository<ErpApplicationDocumentRequest, Long> {

    /**
     * Lists all active document requests for one application in the
     * authenticated branch.
     */
    @EntityGraph(attributePaths = {
            "application",
            "application.branch"
    })
    List<ErpApplicationDocumentRequest>
    findAllByApplication_ApplicationIdAndApplication_Branch_BranchIdAndActiveTrueOrderByRequestedAtDesc(
            Long applicationId,
            Integer branchId
    );

    /**
     * Branch-safe lookup for displaying or processing one request.
     */
    @EntityGraph(attributePaths = {
            "application",
            "application.branch"
    })
    Optional<ErpApplicationDocumentRequest>
    findByRequestIdAndApplication_ApplicationIdAndApplication_Branch_BranchIdAndActiveTrue(
            Long requestId,
            Long applicationId,
            Integer branchId
    );

    /**
     * Locks one request while completing, cancelling or changing its status,
     * preventing concurrent conflicting updates.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select requestRecord
            from ErpApplicationDocumentRequest requestRecord
            join fetch requestRecord.application applicationRecord
            join fetch applicationRecord.branch branchRecord
            where requestRecord.requestId = :requestId
              and applicationRecord.applicationId = :applicationId
              and branchRecord.branchId = :branchId
              and requestRecord.active = true
            """)
    Optional<ErpApplicationDocumentRequest> findForUpdate(
            @Param("requestId") Long requestId,
            @Param("applicationId") Long applicationId,
            @Param("branchId") Integer branchId
    );

    /**
     * Provides branch-level queues such as pending, uploaded, completed,
     * cancelled and expired requests.
     */
    @EntityGraph(attributePaths = {
            "application",
            "application.branch"
    })
    Page<ErpApplicationDocumentRequest>
    findAllByApplication_Branch_BranchIdAndRequestStatusAndActiveTrueOrderByRequestedAtAsc(
            Integer branchId,
            ErpApplicationDocumentRequest.RequestStatus requestStatus,
            Pageable pageable
    );

    /**
     * Returns active requests in one status for application-level workflow
     * validation and status recalculation.
     */
    List<ErpApplicationDocumentRequest>
    findAllByApplication_ApplicationIdAndApplication_Branch_BranchIdAndRequestStatusAndActiveTrueOrderByRequestedAtAsc(
            Long applicationId,
            Integer branchId,
            ErpApplicationDocumentRequest.RequestStatus requestStatus
    );

    /**
     * Resolves a public upload request by the SHA-256 token hash. The service
     * must additionally verify request status, token expiry and deadline.
     */
    @EntityGraph(attributePaths = {
            "application",
            "application.branch"
    })
    Optional<ErpApplicationDocumentRequest>
    findByUploadTokenHashAndActiveTrue(String uploadTokenHash);

    /**
     * Locks a token-backed request before accepting an uploaded file so the
     * same token cannot be consumed concurrently more than once.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select requestRecord
            from ErpApplicationDocumentRequest requestRecord
            join fetch requestRecord.application applicationRecord
            join fetch applicationRecord.branch branchRecord
            where requestRecord.uploadTokenHash = :uploadTokenHash
              and requestRecord.active = true
            """)
    Optional<ErpApplicationDocumentRequest> findByUploadTokenHashForUpdate(
            @Param("uploadTokenHash") String uploadTokenHash
    );

    boolean existsByUploadTokenHash(String uploadTokenHash);

    long countByApplication_ApplicationIdAndApplication_Branch_BranchIdAndRequestStatusAndActiveTrue(
            Long applicationId,
            Integer branchId,
            ErpApplicationDocumentRequest.RequestStatus requestStatus
    );

    /**
     * Finds pending requests whose secure token or upload deadline has expired.
     * A scheduled service can mark these records as EXPIRED.
     */
    @Query("""
            select requestRecord
            from ErpApplicationDocumentRequest requestRecord
            where requestRecord.active = true
              and requestRecord.requestStatus = :pendingStatus
              and (
                    (requestRecord.uploadTokenExpiresAt is not null
                     and requestRecord.uploadTokenExpiresAt < :now)
                 or (requestRecord.uploadDeadline is not null
                     and requestRecord.uploadDeadline < :now)
              )
            order by requestRecord.requestedAt asc
            """)
    List<ErpApplicationDocumentRequest> findExpiredPendingRequests(
            @Param("pendingStatus") ErpApplicationDocumentRequest.RequestStatus pendingStatus,
            @Param("now") LocalDateTime now
    );
}
