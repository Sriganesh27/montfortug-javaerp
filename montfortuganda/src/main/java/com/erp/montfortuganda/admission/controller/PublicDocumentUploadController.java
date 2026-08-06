package com.erp.montfortuganda.admission.controller;

import com.erp.montfortuganda.admission.dto.PublicDocumentUploadInfoDTO;
import com.erp.montfortuganda.admission.dto.PublicDocumentUploadResponseDTO;
import com.erp.montfortuganda.admission.service.PublicDocumentUploadService;
import com.erp.montfortuganda.dto.ApiResponse;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Public, secure-token endpoints for uploading a document requested during
 * admission verification.
 *
 * <p>The token is accepted only in the request body/form data rather than as
 * a path variable. Responses are marked no-store because they contain
 * applicant information.</p>
 */
@RestController
@RequestMapping("/api/public/applications/document-upload")
@PreAuthorize("permitAll()")
public class PublicDocumentUploadController {

    private final PublicDocumentUploadService uploadService;

    public PublicDocumentUploadController(
            PublicDocumentUploadService uploadService
    ) {
        this.uploadService = uploadService;
    }

    /**
     * Validates the secure upload token and returns applicant-safe request
     * information before a file is selected.
     */
    @PostMapping(
            value = "/info",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE
    )
    public ResponseEntity<ApiResponse<PublicDocumentUploadInfoDTO>>
    getUploadInfo(
            @RequestParam("token") String token
    ) {
        PublicDocumentUploadInfoDTO info =
                uploadService.getUploadInfo(token);

        return noStore(
                ApiResponse.success(
                        "Document upload request fetched successfully",
                        info
                )
        );
    }

    /**
     * Returns the selected branch's private logo without exposing its stored
     * path. POST keeps the secure token out of image URLs and browser history.
     */
    @PostMapping(
            value = "/logo",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE
    )
    public ResponseEntity<Resource> getSchoolLogo(
            @RequestParam("token") String token
    ) {
        PublicDocumentUploadService
                .PublicSchoolLogoResource logo =
                uploadService.loadSchoolLogo(token);

        MediaType mediaType;

        try {
            mediaType =
                    MediaType.parseMediaType(
                            logo.contentType()
                    );
        } catch (IllegalArgumentException exception) {
            mediaType =
                    MediaType.APPLICATION_OCTET_STREAM;
        }

        return ResponseEntity.ok()
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
                        HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\""
                                + logo.fileName()
                                + "\""
                )
                .cacheControl(
                        CacheControl.noStore()
                                .mustRevalidate()
                )
                .contentType(mediaType)
                .body(
                        logo.resource()
                );
    }

    /**
     * Uploads one PDF, JPG/JPEG, or PNG file using a pending single-use token.
     */
    @PostMapping(
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ApiResponse<PublicDocumentUploadResponseDTO>>
    uploadRequestedDocument(
            @RequestParam("token") String token,
            @RequestParam("file") MultipartFile file
    ) {
        PublicDocumentUploadResponseDTO uploaded =
                uploadService.uploadRequestedDocument(
                        token,
                        file
                );

        return noStore(
                ApiResponse.success(
                        "Requested document uploaded successfully",
                        uploaded
                )
        );
    }

    private <T> ResponseEntity<T> noStore(
            T body
    ) {
        return ResponseEntity.ok()
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
                .cacheControl(
                        CacheControl.noStore()
                                .mustRevalidate()
                )
                .body(body);
    }
}
