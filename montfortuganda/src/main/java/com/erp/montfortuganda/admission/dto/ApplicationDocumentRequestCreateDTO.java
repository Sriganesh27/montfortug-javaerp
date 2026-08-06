package com.erp.montfortuganda.admission.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Request used by an authorized branch user to ask an applicant
 * for one additional document.
 *
 * <p>One request creates one document-request record. Multiple required
 * documents should be submitted as separate requests so each document can
 * be tracked, uploaded, reviewed, completed, or cancelled independently.</p>
 */
@Data
public class ApplicationDocumentRequestCreateDTO {

    /**
     * Standard document code such as BIRTH_CERTIFICATE, REPORT_CARD,
     * TRANSFER_LETTER, NATIONAL_ID, or OTHER.
     *
     * <p>This remains text to support future or school-specific document
     * types without changing the database structure.</p>
     */
    @NotBlank(message = "Requested document type is required")
    @Size(max = 50, message = "Requested document type must not exceed 50 characters")
    private String requestedDocumentType;

    /**
     * Optional applicant-friendly document name displayed in the email and
     * upload page. When blank, the backend derives a readable name from the
     * selected document type.
     */
    @Size(max = 150, message = "Requested document name must not exceed 150 characters")
    private String requestedDocumentName;

    /**
     * Optional reason explaining why the document is required. When blank,
     * the backend stores a safe default verification reason.
     */
    @Size(max = 1000, message = "Document request reason must not exceed 1000 characters")
    private String requestReason;

    /**
     * Applicant-visible instructions or remarks.
     */
    @Size(max = 1000, message = "Public remarks must not exceed 1000 characters")
    private String publicRemarks;

    /**
     * School-only remarks. These must never be exposed through public APIs.
     */
    @Size(max = 1000, message = "Internal remarks must not exceed 1000 characters")
    private String internalRemarks;

    /**
     * Optional deadline for uploading the requested document.
     */
    @Future(message = "Upload deadline must be in the future")
    private LocalDateTime uploadDeadline;
}
