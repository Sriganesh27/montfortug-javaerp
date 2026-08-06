package com.erp.montfortuganda.admission.dto;

import com.erp.montfortuganda.admission.entity.ErpApplicationDocumentRequest;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Branch-facing response for an additional-document request.
 *
 * <p>The secure upload-token hash is intentionally not exposed. A raw upload
 * link may be returned only by the dedicated request-creation service at the
 * moment the token is generated.</p>
 */
@Data
public class ApplicationDocumentRequestResponseDTO {

    private Long requestId;

    private Long applicationId;

    private String applicationNo;

    private String requestedDocumentType;

    private String requestedDocumentName;

    private String requestReason;

    private String publicRemarks;

    private String internalRemarks;

    private ErpApplicationDocumentRequest.RequestStatus requestStatus;

    private Integer requestedByUserId;

    private LocalDateTime requestedAt;

    private LocalDateTime uploadDeadline;

    /**
     * Expiry time of the secure applicant upload link.
     * The token itself and its stored hash are never returned here.
     */
    private LocalDateTime uploadTokenExpiresAt;

    private LocalDateTime tokenUsedAt;

    private LocalDateTime completedAt;

    private Integer completedByUserId;

    private LocalDateTime cancelledAt;

    private Integer cancelledByUserId;

    private String cancellationReason;

    private Boolean emailRequired;

    private ErpApplicationDocumentRequest.EmailStatus emailStatus;

    private LocalDateTime emailSentAt;

    /**
     * Current uploaded document created for this request, when available.
     */
    private Long uploadedDocumentId;

    private String uploadedDocumentName;

    private LocalDateTime documentUploadedAt;

    private Boolean active;

    private Long version;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
