package com.erp.montfortuganda.admission.dto;

import com.erp.montfortuganda.admission.entity.ErpApplicationDocument;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Branch admission response for an uploaded application document.
 *
 * <p>The physical storage path, stored file name and file hash are
 * intentionally not exposed. Documents must be opened or downloaded only
 * through the secured admission document endpoints.</p>
 */
@Data
public class ApplicationDocumentResponseDTO {

    private Long documentId;
    private Long applicationId;
    private Long documentRequestId;

    private ErpApplicationDocument.DocumentType documentType;
    private ErpApplicationDocument.SubmissionSource submissionSource;
    private ErpApplicationDocument.VerificationStatus verificationStatus;

    private Boolean current;
    private Boolean active;

    private String originalFileName;
    private Long fileSize;
    private String contentType;
    private LocalDateTime uploadedAt;
    private LocalDateTime updatedAt;

    private Integer verifiedByUserId;
    private LocalDateTime verifiedAt;

    private Integer rejectedByUserId;
    private LocalDateTime rejectedAt;
    private String rejectionReason;

    private String publicRemarks;
    private String internalRemarks;

    private LocalDateTime reuploadRequestedAt;
    private LocalDateTime reuploadDeadline;

    private Long replacementDocumentId;
    private LocalDateTime supersededAt;
    private Integer supersededByUserId;
}
