package com.erp.montfortuganda.student.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

/**
 * Multipart request used to upload or replace a Student profile photo.
 *
 * Student ID, branch, stored file name, file path, MIME type,
 * extension, file size and audit information are controlled
 * entirely by the backend.
 */
@SuppressWarnings("unused")
public record StudentPhotoUploadRequest(

        MultipartFile photo,

        /**
         * Must match erp_students.version.
         */
        Long version,

        /**
         * Prevents duplicate photo-upload processing caused by
         * repeated clicks or network retries.
         */
        @NotBlank(message = "Operation ID is required.")
        @Size(
                max = 100,
                message = "Operation ID cannot exceed 100 characters."
        )
        String operationId

) {

    @AssertTrue(message = "Select a valid Student photo.")
    public boolean isPhotoProvided() {
        return photo != null && !photo.isEmpty();
    }

    @AssertTrue(message = "Student record version is required.")
    public boolean isVersionProvided() {
        return version != null;
    }
}