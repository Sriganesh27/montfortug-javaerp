package com.erp.montfortuganda.employee.dto.request;

import com.erp.montfortuganda.employee.enums.EmployeeDocumentType;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Employee-document data submitted during Employee registration or update.

 * Client-editable document details are accepted here. Stored file names,
 * permanent file paths, file extensions, verification ownership, verification
 * timestamps, audit fields and entity versions are controlled by the backend.

 * For standard document types, the backend generates the display name from
 * the selected enum value. A custom name is required only for type OTHER.
 */
@SuppressWarnings("unused")
public record EmployeeDocumentRequest(

        @Positive(message = "Employee document ID must be greater than zero.")
        Long employeeDocumentId,

        @NotNull(message = "Employee document type is required.")
        EmployeeDocumentType employeeDocumentType,

        @Size(
                max = 255,
                message = "Employee document name cannot exceed 255 characters."
        )
        String employeeDocumentName,

        @Size(
                max = 10000,
                message = "Employee document description cannot exceed 10000 characters."
        )
        String employeeDocumentDescription,

        LocalDate employeeDocumentIssueDate,

        LocalDate employeeDocumentExpiryDate,

        Boolean employeeDocumentIsMandatory,

        Boolean employeeDocumentActive,

        @Size(
                max = 10000,
                message = "Employee document remarks cannot exceed 10000 characters."
        )
        String employeeDocumentRemarks,

        String fileData,

        @Size(
                max = 255,
                message = "Employee document file name cannot exceed 255 characters."
        )
        String fileName,

        @Size(
                max = 100,
                message = "Employee document content type cannot exceed 100 characters."
        )
        String contentType,

        @Positive(message = "Employee document file size must be greater than zero.")
        Long fileSize
) {

    @AssertTrue(
            message = "Document name is required when document type is OTHER."
    )
    public boolean isDocumentNameValid() {
        return employeeDocumentType != EmployeeDocumentType.OTHER
                || hasText(employeeDocumentName);
    }

    @AssertTrue(
            message = "Employee document expiry date cannot be earlier than the issue date."
    )
    public boolean isDateRangeValid() {
        return employeeDocumentIssueDate == null
                || employeeDocumentExpiryDate == null
                || !employeeDocumentExpiryDate.isBefore(
                employeeDocumentIssueDate
        );
    }

    @AssertTrue(
            message = "A new employee document requires file data, file name, content type and file size."
    )
    public boolean isNewDocumentFileValid() {
        return employeeDocumentId != null
                || (
                hasText(fileData)
                        && hasText(fileName)
                        && hasText(contentType)
                        && fileSize != null
        );
    }

    @AssertTrue(
            message = "Replacement document file data and metadata must be supplied together."
    )
    public boolean isReplacementFileMetadataValid() {
        if (employeeDocumentId == null) {
            return true;
        }

        boolean anyFileValueSupplied =
                hasText(fileData)
                        || hasText(fileName)
                        || hasText(contentType)
                        || fileSize != null;

        return !anyFileValueSupplied
                || (
                hasText(fileData)
                        && hasText(fileName)
                        && hasText(contentType)
                        && fileSize != null
        );
    }

    private boolean hasText(
            String value
    ) {
        return value != null
                && !value.isBlank();
    }
}