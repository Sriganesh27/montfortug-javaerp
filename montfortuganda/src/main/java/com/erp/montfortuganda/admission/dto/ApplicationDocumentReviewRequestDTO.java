package com.erp.montfortuganda.admission.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Request used by an authorized branch user to review an application
 * document.
 *
 * <p>Decision-specific validation is completed in the service layer:</p>
 * <ul>
 *     <li>{@code REJECT} requires a rejection reason.</li>
 *     <li>{@code REQUEST_REUPLOAD} requires a request reason.</li>
 * </ul>
 */
@Data
public class ApplicationDocumentReviewRequestDTO {

    public enum ReviewDecision {
        VERIFY,
        REJECT,
        REQUEST_REUPLOAD
    }

    @NotNull(message = "Document review decision is required")
    private ReviewDecision decision;

    /**
     * Applicant-visible explanation or instruction.
     */
    @Size(max = 1000, message = "Public remarks must not exceed 1000 characters")
    private String publicRemarks;

    /**
     * School-only remarks. These must never be returned through public APIs.
     */
    @Size(max = 1000, message = "Internal remarks must not exceed 1000 characters")
    private String internalRemarks;

    /**
     * Required when decision is REJECT.
     */
    @Size(max = 1000, message = "Rejection reason must not exceed 1000 characters")
    private String rejectionReason;

    /**
     * Required when decision is REQUEST_REUPLOAD.
     */
    @Size(max = 1000, message = "Re-upload request reason must not exceed 1000 characters")
    private String reuploadReason;

    /**
     * Optional deadline for the applicant to upload a replacement document.
     */
    private LocalDateTime reuploadDeadline;
}
