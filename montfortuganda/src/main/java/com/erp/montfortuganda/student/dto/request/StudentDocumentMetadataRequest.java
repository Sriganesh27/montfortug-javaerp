package com.erp.montfortuganda.student.dto.request;

import com.erp.montfortuganda.student.enums.StudentDocumentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Metadata entered by the user while uploading a Student document.
 *
 * File name, original file name, stored path, extension, MIME type,
 * file size, upload information, verification information, branch,
 * Student, admission number, status and audit fields are controlled
 * by the backend.
 */
@SuppressWarnings("unused")
public record StudentDocumentMetadataRequest(

        @NotNull(message = "Document type is required.")
        StudentDocumentType documentType,

        @NotBlank(message = "Document name is required.")
        @Size(
                max = 150,
                message = "Document name cannot exceed 150 characters."
        )
        String documentName,

        @Size(
                max = 100,
                message = "Document number cannot exceed 100 characters."
        )
        String documentNumber,

        @Size(
                max = 5000,
                message = "Document remarks cannot exceed 5000 characters."
        )
        String remarks
) {
}