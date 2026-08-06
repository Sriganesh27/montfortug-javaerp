package com.erp.montfortuganda.admission.mapper;

import com.erp.montfortuganda.admission.dto.ApplicationDocumentRequestResponseDTO;
import com.erp.montfortuganda.admission.dto.ApplicationDocumentResponseDTO;
import com.erp.montfortuganda.admission.entity.ErpApplication;
import com.erp.montfortuganda.admission.entity.ErpApplicationDocument;
import com.erp.montfortuganda.admission.entity.ErpApplicationDocumentRequest;
import org.springframework.stereotype.Component;

/**
 * Maps admission document entities to branch-facing response DTOs.
 *
 * <p>Storage paths, stored file names, file hashes, and secure upload-token
 * hashes are intentionally never copied into API responses.</p>
 */
@Component
public class ApplicationDocumentMapper {

    public ApplicationDocumentResponseDTO toDocumentResponse(
            ErpApplicationDocument document
    ) {
        if (document == null) {
            return null;
        }

        ApplicationDocumentResponseDTO response =
                new ApplicationDocumentResponseDTO();

        response.setDocumentId(document.getDocumentId());

        ErpApplication application = document.getApplication();
        response.setApplicationId(
                application != null ? application.getApplicationId() : null
        );

        ErpApplicationDocumentRequest documentRequest =
                document.getDocumentRequest();
        response.setDocumentRequestId(
                documentRequest != null
                        ? documentRequest.getRequestId()
                        : null
        );

        response.setDocumentType(document.getDocumentType());
        response.setSubmissionSource(document.getSubmissionSource());
        response.setVerificationStatus(document.getVerificationStatus());

        response.setCurrent(document.getCurrent());
        response.setActive(document.getActive());

        response.setOriginalFileName(document.getOriginalFileName());
        response.setFileSize(document.getFileSize());
        response.setContentType(document.getContentType());
        response.setUploadedAt(document.getUploadedAt());
        response.setUpdatedAt(document.getUpdatedAt());

        response.setVerifiedByUserId(document.getVerifiedByUserId());
        response.setVerifiedAt(document.getVerifiedAt());

        response.setRejectedByUserId(document.getRejectedByUserId());
        response.setRejectedAt(document.getRejectedAt());
        response.setRejectionReason(document.getRejectionReason());

        response.setPublicRemarks(document.getPublicRemarks());
        response.setInternalRemarks(document.getInternalRemarks());

        response.setReuploadRequestedAt(document.getReuploadRequestedAt());
        response.setReuploadDeadline(document.getReuploadDeadline());

        ErpApplicationDocument replacementDocument =
                document.getReplacementDocument();
        response.setReplacementDocumentId(
                replacementDocument != null
                        ? replacementDocument.getDocumentId()
                        : null
        );

        response.setSupersededAt(document.getSupersededAt());
        response.setSupersededByUserId(
                document.getSupersededByUserId()
        );

        return response;
    }

    public ApplicationDocumentRequestResponseDTO toDocumentRequestResponse(
            ErpApplicationDocumentRequest request
    ) {
        return toDocumentRequestResponse(request, null);
    }

    /**
     * Maps one additional-document request and, when already uploaded,
     * includes only safe metadata about the document created for that request.
     */
    public ApplicationDocumentRequestResponseDTO toDocumentRequestResponse(
            ErpApplicationDocumentRequest request,
            ErpApplicationDocument uploadedDocument
    ) {
        if (request == null) {
            return null;
        }

        ApplicationDocumentRequestResponseDTO response =
                new ApplicationDocumentRequestResponseDTO();

        response.setRequestId(request.getRequestId());

        ErpApplication application = request.getApplication();
        if (application != null) {
            response.setApplicationId(application.getApplicationId());
            response.setApplicationNo(application.getApplicationNo());
        }

        response.setRequestedDocumentType(
                request.getRequestedDocumentType()
        );
        response.setRequestedDocumentName(
                request.getRequestedDocumentName()
        );
        response.setRequestReason(request.getRequestReason());

        response.setPublicRemarks(request.getPublicRemarks());
        response.setInternalRemarks(request.getInternalRemarks());

        response.setRequestStatus(request.getRequestStatus());
        response.setRequestedByUserId(
                request.getRequestedByUserId()
        );
        response.setRequestedAt(request.getRequestedAt());
        response.setUploadDeadline(request.getUploadDeadline());
        response.setUploadTokenExpiresAt(
                request.getUploadTokenExpiresAt()
        );
        response.setTokenUsedAt(request.getTokenUsedAt());

        response.setCompletedAt(request.getCompletedAt());
        response.setCompletedByUserId(
                request.getCompletedByUserId()
        );

        response.setCancelledAt(request.getCancelledAt());
        response.setCancelledByUserId(
                request.getCancelledByUserId()
        );
        response.setCancellationReason(
                request.getCancellationReason()
        );

        response.setEmailRequired(request.getEmailRequired());
        response.setEmailStatus(request.getEmailStatus());
        response.setEmailSentAt(request.getEmailSentAt());

        if (uploadedDocument != null) {
            response.setUploadedDocumentId(
                    uploadedDocument.getDocumentId()
            );
            response.setUploadedDocumentName(
                    uploadedDocument.getOriginalFileName()
            );
            response.setDocumentUploadedAt(
                    uploadedDocument.getUploadedAt()
            );
        }

        response.setActive(request.getActive());
        response.setVersion(request.getVersion());
        response.setCreatedAt(request.getCreatedAt());
        response.setUpdatedAt(request.getUpdatedAt());

        return response;
    }
}
