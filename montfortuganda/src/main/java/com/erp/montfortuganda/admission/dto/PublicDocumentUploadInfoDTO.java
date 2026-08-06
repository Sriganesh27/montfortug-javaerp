package com.erp.montfortuganda.admission.dto;

import com.erp.montfortuganda.admission.entity.ErpApplicationDocumentRequest;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Public, token-protected information shown before an applicant uploads one
 * requested admission document.
 *
 * <p>This response deliberately excludes database IDs used internally,
 * physical storage paths, stored file names, file hashes, token hashes,
 * internal school remarks, and user audit identifiers.</p>
 */
@Data
public class PublicDocumentUploadInfoDTO {

    private String applicationNo;

    private String studentName;

    private String schoolName;

    private String schoolCode;

    private String requestedDocumentType;

    private String requestedDocumentName;

    private String requestReason;

    private String publicRemarks;

    private ErpApplicationDocumentRequest.RequestStatus requestStatus;

    private LocalDateTime requestedAt;

    private LocalDateTime uploadDeadline;

    private LocalDateTime uploadTokenExpiresAt;

    /**
     * True only while the request is active, pending, unused, and within both
     * the token-expiry time and optional school upload deadline.
     */
    private Boolean uploadAllowed;

    /**
     * Applicant-facing explanation when upload is unavailable, such as
     * already uploaded, cancelled, completed, or expired.
     */
    private String uploadUnavailableReason;
}
