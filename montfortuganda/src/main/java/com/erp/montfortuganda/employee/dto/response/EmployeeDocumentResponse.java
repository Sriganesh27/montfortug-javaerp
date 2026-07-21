package com.erp.montfortuganda.employee.dto.response;

import com.erp.montfortuganda.employee.enums.EmployeeDocumentType;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Complete Employee document data returned by authenticated, branch-scoped
 * Employee APIs.
 * Private storage paths and generated internal file names are never exposed.
 * The actual file is opened only through a secured backend endpoint after
 * branch and Employee ownership checks.
 */
@SuppressWarnings("unused")
public record EmployeeDocumentResponse(

        Long employeeDocumentId,

        Long employeeId,

        EmployeeDocumentType employeeDocumentType,

        String employeeDocumentName,

        String employeeDocumentDescription,

        String employeeDocumentOriginalFileName,

        String employeeDocumentFileExtension,

        String employeeDocumentMimeType,

        Long employeeDocumentFileSize,

        Boolean employeeDocumentAvailable,

        LocalDate employeeDocumentIssueDate,

        LocalDate employeeDocumentExpiryDate,

        Boolean employeeDocumentVerified,

        Integer employeeDocumentVerifiedById,

        String employeeDocumentVerifiedByUsername,

        LocalDateTime employeeDocumentVerifiedAt,

        Boolean employeeDocumentIsMandatory,

        Boolean employeeDocumentActive,

        String employeeDocumentRemarks,

        Long version,

        String createdBy,

        LocalDateTime createdAt,

        String updatedBy,

        LocalDateTime updatedAt
) {
}