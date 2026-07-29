package com.erp.montfortuganda.student.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

/**
 * Multipart request used to upload a Student document.
 *
 * Student ID and branch are resolved by the backend.
 * File metadata such as stored path, MIME type, extension and size
 * are determined from the uploaded file by the document service.
 */
@SuppressWarnings("unused")
public record StudentDocumentUploadRequest(

        @NotNull(message = "Document metadata is required.")
        @Valid
        StudentDocumentMetadataRequest metadata,

        @NotNull(message = "Document file is required.")
        MultipartFile file

) {

    @AssertTrue(message = "Select a valid document file.")
    public boolean isFileProvided() {
        return file != null && !file.isEmpty();
    }
}
