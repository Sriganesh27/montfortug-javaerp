package com.erp.montfortuganda.admission.service;

import com.erp.montfortuganda.admission.dto.ApplicationDocumentRequestCancelDTO;
import com.erp.montfortuganda.admission.dto.ApplicationDocumentRequestCreateDTO;
import com.erp.montfortuganda.admission.dto.ApplicationDocumentRequestResponseDTO;
import com.erp.montfortuganda.admission.dto.ApplicationDocumentResponseDTO;
import com.erp.montfortuganda.admission.dto.ApplicationDocumentReviewRequestDTO;
import com.erp.montfortuganda.auth.service.CurrentUserContext;
import org.springframework.core.io.Resource;

import java.util.List;

/**
 * Branch-protected business operations for admission-application documents.
 *
 * <p>Every operation must validate that the requested application belongs to
 * the authenticated user's branch. Physical file paths, stored file names,
 * file hashes, and upload-token hashes must never be exposed to clients.</p>
 */
public interface ApplicationDocumentService {

    /**
     * Returns all active documents currently associated with one application
     * in the authenticated branch.
     */
    List<ApplicationDocumentResponseDTO> getApplicationDocuments(
            CurrentUserContext context,
            Long applicationId
    );

    /**
     * Returns safe metadata for one application document after branch
     * ownership has been validated.
     */
    ApplicationDocumentResponseDTO getApplicationDocument(
            CurrentUserContext context,
            Long applicationId,
            Long documentId
    );

    /**
     * Loads one private application document for secure inline viewing or
     * download after validating application and branch ownership.
     */
    ApplicationDocumentFile loadApplicationDocument(
            CurrentUserContext context,
            Long applicationId,
            Long documentId
    );

    /**
     * Verifies, rejects, or requests re-upload of one current document.
     *
     * <p>The implementation must lock the document during the decision,
     * preserve review history, and recalculate the application-level document
     * status in the same transaction.</p>
     */
    ApplicationDocumentResponseDTO reviewDocument(
            CurrentUserContext context,
            Long applicationId,
            Long documentId,
            ApplicationDocumentReviewRequestDTO request
    );

    /**
     * Creates one additional-document request and generates a single-use,
     * time-limited upload token.
     *
     * <p>The raw token may be used only to construct the applicant link sent
     * by email. Only its cryptographic hash may be stored in the database.</p>
     */
    ApplicationDocumentRequestResponseDTO createDocumentRequest(
            CurrentUserContext context,
            Long applicationId,
            ApplicationDocumentRequestCreateDTO request
    );

    /**
     * Returns all active additional-document requests for one application in
     * the authenticated branch.
     */
    List<ApplicationDocumentRequestResponseDTO> getDocumentRequests(
            CurrentUserContext context,
            Long applicationId
    );

    /**
     * Returns one branch-owned additional-document request.
     */
    ApplicationDocumentRequestResponseDTO getDocumentRequest(
            CurrentUserContext context,
            Long applicationId,
            Long requestId
    );

    /**
     * Cancels an outstanding additional-document request with a mandatory
     * auditable reason.
     */
    ApplicationDocumentRequestResponseDTO cancelDocumentRequest(
            CurrentUserContext context,
            Long applicationId,
            Long requestId,
            ApplicationDocumentRequestCancelDTO request
    );

    /**
     * Protected private-file result used by the controller to build a
     * no-cache inline or attachment response.
     */
    record ApplicationDocumentFile(
            Resource resource,
            String fileName,
            String contentType,
            long fileSize
    ) {
    }
}
