package com.erp.montfortuganda.admission.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request used by an authorized branch administrator to delete one
 * unnecessary application document.
 *
 * <p>The application itself is never deleted. The service must use this
 * request only for a branch-scoped document operation, preserve an audit
 * record, mark the document inactive, and remove the stored file safely
 * after the database transaction commits.</p>
 */
@Data
public class ApplicationDocumentDeleteRequestDTO {

    public enum DeletionReason {
        DUPLICATE,
        INCORRECT_UPLOAD,
        NOT_REQUIRED,
        REPLACED,
        OTHER
    }

    /**
     * Controlled reason used for reporting and audit history.
     */
    @NotNull(message = "Document deletion reason is required")
    private DeletionReason deletionReason;

    /**
     * Mandatory school-only explanation. This value must never be exposed
     * through public application APIs or applicant emails.
     */
    @NotBlank(message = "Document deletion details are required")
    @Size(
            max = 1000,
            message = "Document deletion details must not exceed 1000 characters"
    )
    private String deletionDetails;
}
