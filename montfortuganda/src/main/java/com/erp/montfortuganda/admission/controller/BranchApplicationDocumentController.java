package com.erp.montfortuganda.admission.controller;

import com.erp.montfortuganda.admission.dto.ApplicationDocumentDeleteRequestDTO;
import com.erp.montfortuganda.admission.dto.ApplicationDocumentRequestCancelDTO;
import com.erp.montfortuganda.admission.dto.ApplicationDocumentRequestCreateDTO;
import com.erp.montfortuganda.admission.dto.ApplicationDocumentRequestResponseDTO;
import com.erp.montfortuganda.admission.dto.ApplicationDocumentResponseDTO;
import com.erp.montfortuganda.admission.dto.ApplicationDocumentReviewRequestDTO;
import com.erp.montfortuganda.admission.service.ApplicationDocumentService;
import com.erp.montfortuganda.admission.service.ApplicationDocumentService.ApplicationDocumentFile;
import com.erp.montfortuganda.auth.service.CurrentUserContext;
import com.erp.montfortuganda.auth.service.CurrentUserService;
import com.erp.montfortuganda.dto.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Secure branch-scoped access to public admission documents.
 *
 * <p>The browser never receives a physical storage path or stored file name.
 * Every operation validates the authenticated Branch Admin and application
 * branch before metadata, review, request, file access, or deletion.</p>
 */
@RestController
@RequestMapping(
        "/api/admission/branch/applications/{applicationId}/documents"
)
@PreAuthorize("hasRole('BRANCH_ADMIN')")
public class BranchApplicationDocumentController {

    private final ApplicationDocumentService documentService;
    private final CurrentUserService currentUserService;

    public BranchApplicationDocumentController(
            ApplicationDocumentService documentService,
            CurrentUserService currentUserService
    ) {
        this.documentService = documentService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public ResponseEntity<
            ApiResponse<List<ApplicationDocumentResponseDTO>>>
    getApplicationDocuments(
            Authentication authentication,
            @PathVariable Long applicationId
    ) {
        CurrentUserContext context =
                currentUserService.getCurrentUserContext(
                        authentication
                );

        List<ApplicationDocumentResponseDTO> documents =
                documentService.getApplicationDocuments(
                        context,
                        applicationId
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Application documents fetched successfully",
                        documents
                )
        );
    }

    @GetMapping("/{documentId}")
    public ResponseEntity<
            ApiResponse<ApplicationDocumentResponseDTO>>
    getApplicationDocument(
            Authentication authentication,
            @PathVariable Long applicationId,
            @PathVariable Long documentId
    ) {
        CurrentUserContext context =
                currentUserService.getCurrentUserContext(
                        authentication
                );

        ApplicationDocumentResponseDTO document =
                documentService.getApplicationDocument(
                        context,
                        applicationId,
                        documentId
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Application document fetched successfully",
                        document
                )
        );
    }

    /**
     * Verifies, rejects, or requests re-upload of one current document.
     */
    @RequestMapping(
            path = "/{documentId}/review",
            method = {
                    RequestMethod.PATCH,
                    RequestMethod.POST
            }
    )
    public ResponseEntity<
            ApiResponse<ApplicationDocumentResponseDTO>>
    reviewApplicationDocument(
            Authentication authentication,
            @PathVariable Long applicationId,
            @PathVariable Long documentId,
            @Valid @RequestBody
            ApplicationDocumentReviewRequestDTO request
    ) {
        CurrentUserContext context =
                currentUserService.getCurrentUserContext(
                        authentication
                );

        ApplicationDocumentResponseDTO document =
                documentService.reviewDocument(
                        context,
                        applicationId,
                        documentId,
                        request
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Application document reviewed successfully",
                        document
                )
        );
    }

    /**
     * Creates a secure additional-document request for the applicant.
     */
    @PostMapping("/requests")
    public ResponseEntity<
            ApiResponse<ApplicationDocumentRequestResponseDTO>>
    createApplicationDocumentRequest(
            Authentication authentication,
            @PathVariable Long applicationId,
            @Valid @RequestBody
            ApplicationDocumentRequestCreateDTO request
    ) {
        CurrentUserContext context =
                currentUserService.getCurrentUserContext(
                        authentication
                );

        ApplicationDocumentRequestResponseDTO documentRequest =
                documentService.createDocumentRequest(
                        context,
                        applicationId,
                        request
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Additional-document request created successfully",
                        documentRequest
                )
        );
    }

    /**
     * Returns all active additional-document requests for one application.
     */
    @GetMapping("/requests")
    public ResponseEntity<
            ApiResponse<List<ApplicationDocumentRequestResponseDTO>>>
    getApplicationDocumentRequests(
            Authentication authentication,
            @PathVariable Long applicationId
    ) {
        CurrentUserContext context =
                currentUserService.getCurrentUserContext(
                        authentication
                );

        List<ApplicationDocumentRequestResponseDTO> requests =
                documentService.getDocumentRequests(
                        context,
                        applicationId
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Additional-document requests fetched successfully",
                        requests
                )
        );
    }

    /**
     * Returns one additional-document request after branch validation.
     */
    @GetMapping("/requests/{requestId}")
    public ResponseEntity<
            ApiResponse<ApplicationDocumentRequestResponseDTO>>
    getApplicationDocumentRequest(
            Authentication authentication,
            @PathVariable Long applicationId,
            @PathVariable Long requestId
    ) {
        CurrentUserContext context =
                currentUserService.getCurrentUserContext(
                        authentication
                );

        ApplicationDocumentRequestResponseDTO request =
                documentService.getDocumentRequest(
                        context,
                        applicationId,
                        requestId
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Additional-document request fetched successfully",
                        request
                )
        );
    }

    /**
     * Cancels one outstanding additional-document request.
     */
    @PatchMapping("/requests/{requestId}/cancel")
    public ResponseEntity<
            ApiResponse<ApplicationDocumentRequestResponseDTO>>
    cancelApplicationDocumentRequest(
            Authentication authentication,
            @PathVariable Long applicationId,
            @PathVariable Long requestId,
            @Valid @RequestBody
            ApplicationDocumentRequestCancelDTO request
    ) {
        CurrentUserContext context =
                currentUserService.getCurrentUserContext(
                        authentication
                );

        ApplicationDocumentRequestResponseDTO cancelledRequest =
                documentService.cancelDocumentRequest(
                        context,
                        applicationId,
                        requestId,
                        request
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Additional-document request cancelled successfully",
                        cancelledRequest
                )
        );
    }

    @GetMapping("/{documentId}/view")
    public ResponseEntity<Resource> viewApplicationDocument(
            Authentication authentication,
            @PathVariable Long applicationId,
            @PathVariable Long documentId
    ) {
        CurrentUserContext context =
                currentUserService.getCurrentUserContext(
                        authentication
                );

        ApplicationDocumentFile file =
                documentService.loadApplicationDocument(
                        context,
                        applicationId,
                        documentId
                );

        return buildFileResponse(
                file,
                true
        );
    }

    @GetMapping("/{documentId}/download")
    public ResponseEntity<Resource> downloadApplicationDocument(
            Authentication authentication,
            @PathVariable Long applicationId,
            @PathVariable Long documentId
    ) {
        CurrentUserContext context =
                currentUserService.getCurrentUserContext(
                        authentication
                );

        ApplicationDocumentFile file =
                documentService.loadApplicationDocument(
                        context,
                        applicationId,
                        documentId
                );

        return buildFileResponse(
                file,
                false
        );
    }

    /**
     * Deactivates one unnecessary document. The application and document
     * audit row remain; only the public-upload file is removed after commit.
     */
    @DeleteMapping("/{documentId}")
    public ResponseEntity<ApiResponse<Boolean>>
    deleteApplicationDocument(
            Authentication authentication,
            @PathVariable Long applicationId,
            @PathVariable Long documentId,
            @Valid @RequestBody
            ApplicationDocumentDeleteRequestDTO request
    ) {
        CurrentUserContext context =
                currentUserService.getCurrentUserContext(
                        authentication
                );

        documentService.deleteDocument(
                context,
                applicationId,
                documentId,
                request
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Application document deleted successfully",
                        Boolean.TRUE
                )
        );
    }

    private ResponseEntity<Resource> buildFileResponse(
            ApplicationDocumentFile file,
            boolean inline
    ) {
        MediaType mediaType =
                parseMediaType(
                        file.contentType()
                );

        ContentDisposition disposition =
                inline
                        ? ContentDisposition.inline()
                                .filename(
                                        file.fileName(),
                                        StandardCharsets.UTF_8
                                )
                                .build()
                        : ContentDisposition.attachment()
                                .filename(
                                        file.fileName(),
                                        StandardCharsets.UTF_8
                                )
                                .build();

        return ResponseEntity.ok()
                .contentType(mediaType)
                .contentLength(file.fileSize())
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        disposition.toString()
                )
                .header(
                        HttpHeaders.PRAGMA,
                        "no-cache"
                )
                .header(
                        HttpHeaders.EXPIRES,
                        "0"
                )
                .header(
                        "X-Content-Type-Options",
                        "nosniff"
                )
                .header(
                        "X-Frame-Options",
                        "SAMEORIGIN"
                )
                .cacheControl(
                        CacheControl.noStore()
                                .mustRevalidate()
                )
                .body(file.resource());
    }

    private MediaType parseMediaType(
            String contentType
    ) {
        if (contentType == null
                || contentType.isBlank()) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }

        try {
            return MediaType.parseMediaType(
                    contentType
            );
        } catch (InvalidMediaTypeException exception) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }
}
