package com.erp.montfortuganda.admission.dto;

import com.erp.montfortuganda.admission.entity.ErpApplicationDocument;
import com.erp.montfortuganda.admission.entity.ErpApplicationDocumentRequest;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Public confirmation returned after a requested admission document has been
 * uploaded successfully using its secure single-use token.
 *
 * <p>This response does not expose database identifiers, physical storage
 * paths, stored file names, file hashes, token hashes, or internal remarks.</p>
 */
@Data
public class PublicDocumentUploadResponseDTO {

    private String applicationNo;

    private String studentName;

    private String schoolName;

    private String requestedDocumentName;

    private String uploadedFileName;

    private String contentType;

    private Long fileSize;

    private LocalDateTime uploadedAt;

    private ErpApplicationDocumentRequest.RequestStatus requestStatus;

    private ErpApplicationDocument.VerificationStatus verificationStatus;

    private String message;
}
