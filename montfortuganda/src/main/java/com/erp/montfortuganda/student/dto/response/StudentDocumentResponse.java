package com.erp.montfortuganda.student.dto.response;

import java.time.LocalDateTime;

/**
 * Safe response returned for a Student document.
 *
 * The physical server file path and stored internal filename are not exposed.
 * The frontend receives a protected preview/download URL instead.
 */
public record StudentDocumentResponse(

        Long documentId,

        Long studentId,

        String admissionNo,

        Integer branchId,

        String documentType,

        String documentName,

        String documentNumber,

        String originalFileName,

        String fileExtension,

        String mimeType,

        Long fileSize,

        String documentStatus,

        String remarks,

        /**
         * Protected endpoint used to preview or download the document.
         *
         * Example:
         * /api/students/{studentId}/documents/{documentId}/download
         */
        String documentUrl,

        Long uploadedBy,

        String uploadedByName,

        LocalDateTime uploadedAt,

        Long verifiedBy,

        String verifiedByName,

        LocalDateTime verifiedAt,

        Boolean active,

        LocalDateTime createdAt,

        LocalDateTime updatedAt

) {
}