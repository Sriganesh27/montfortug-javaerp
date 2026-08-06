package com.erp.montfortuganda.admission.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request used by an authorized branch user to cancel an outstanding
 * additional-document request.
 */
@Data
public class ApplicationDocumentRequestCancelDTO {

    /**
     * Mandatory internal reason recorded in the document-request audit trail.
     */
    @NotBlank(message = "Cancellation reason is required")
    @Size(max = 1000, message = "Cancellation reason must not exceed 1000 characters")
    private String cancellationReason;
}
