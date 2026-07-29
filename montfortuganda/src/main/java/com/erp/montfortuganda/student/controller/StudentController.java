package com.erp.montfortuganda.student.controller;

import com.erp.montfortuganda.common.response.ApiResponse;
import com.erp.montfortuganda.student.dto.request.StudentCreateRequest;
import com.erp.montfortuganda.student.dto.request.StudentDocumentMetadataRequest;
import com.erp.montfortuganda.student.dto.request.StudentDocumentUploadRequest;
import com.erp.montfortuganda.student.dto.request.StudentDocumentVerificationRequest;
import com.erp.montfortuganda.student.dto.request.StudentEnrollmentUpdateRequest;
import com.erp.montfortuganda.student.dto.request.StudentListFilterRequest;
import com.erp.montfortuganda.student.dto.request.StudentPhotoUploadRequest;
import com.erp.montfortuganda.student.dto.request.StudentStatusChangeRequest;
import com.erp.montfortuganda.student.dto.request.StudentUpdateRequest;
import com.erp.montfortuganda.student.dto.response.PagedStudentResponse;
import com.erp.montfortuganda.student.dto.response.StudentCreateResponse;
import com.erp.montfortuganda.student.dto.response.StudentDocumentResponse;
import com.erp.montfortuganda.student.dto.response.StudentEnrollmentResponse;
import com.erp.montfortuganda.student.dto.response.StudentPersonalResponse;
import com.erp.montfortuganda.student.dto.response.StudentProfileResponse;
import com.erp.montfortuganda.student.dto.response.StudentReferenceDataResponse;
import com.erp.montfortuganda.student.service.StudentDocumentService;
import com.erp.montfortuganda.student.service.StudentDocumentService.StudentDocumentFile;
import com.erp.montfortuganda.student.service.StudentPhotoService;
import com.erp.montfortuganda.student.service.StudentPhotoService.StudentPhotoFile;
import com.erp.montfortuganda.student.service.StudentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

/**
 * Branch-protected HTTP endpoints for the Student module.
 *
 * <p>Student PII, medical information, parent details and private files are
 * always returned with no-store cache headers.</p>
 */
@SuppressWarnings("unused")
@Validated
@RestController
@RequestMapping("/api/students")
@PreAuthorize("hasRole('BRANCH_ADMIN')")
public class StudentController {

    private static final CacheControl NO_STORE =
            CacheControl.noStore()
                    .mustRevalidate();

    private static final int MAX_PAGE_SIZE =
            100;

    private final StudentService studentService;
    private final StudentDocumentService documentService;
    private final StudentPhotoService photoService;

    public StudentController(
            StudentService studentService,
            StudentDocumentService documentService,
            StudentPhotoService photoService
    ) {
        this.studentService = studentService;
        this.documentService = documentService;
        this.photoService = photoService;
    }

    // =====================================================================
    // STUDENT REGISTRATION
    // =====================================================================

    @PostMapping("/registrations")
    public ResponseEntity<ApiResponse<StudentCreateResponse>>
    createStudent(
            @Valid
            @RequestBody
            StudentCreateRequest request
    ) {
        StudentCreateResponse response =
                studentService.createStudent(
                        request
                );

        return noStore(
                ApiResponse.success(
                        "Student registered successfully.",
                        response
                )
        );
    }


    // =====================================================================
    // STUDENT REFERENCE DATA
    // =====================================================================

    @GetMapping("/reference-data")
    public ResponseEntity<ApiResponse<StudentReferenceDataResponse>>
    getReferenceData() {
        StudentReferenceDataResponse response =
                studentService.getReferenceData();

        return noStore(
                ApiResponse.success(
                        "Student reference data fetched successfully.",
                        response
                )
        );
    }

    // =====================================================================
    // STUDENT PROFILE
    // =====================================================================

    @GetMapping("/{studentId:\\d+}")
    public ResponseEntity<ApiResponse<StudentProfileResponse>>
    getStudentProfile(
            @PathVariable
            @Positive(
                    message =
                            "Student ID must be greater than zero."
            )
            Long studentId
    ) {
        StudentProfileResponse response =
                studentService.getStudentProfile(
                        studentId
                );

        return noStore(
                ApiResponse.success(
                        "Student profile fetched successfully.",
                        response
                )
        );
    }

    // =====================================================================
    // STUDENT LIST
    // =====================================================================

    @PostMapping("/search")
    public ResponseEntity<ApiResponse<PagedStudentResponse>>
    searchStudents(
            @Valid
            @RequestBody(
                    required = false
            )
            StudentListFilterRequest filter,

            @RequestParam(
                    defaultValue = "0"
            )
            @PositiveOrZero(
                    message =
                            "Page number cannot be negative."
            )
            Integer page,

            @RequestParam(
                    defaultValue = "20"
            )
            @Positive(
                    message =
                            "Page size must be greater than zero."
            )
            Integer size,

            @RequestParam(
                    defaultValue = "studentId,desc"
            )
            String sort
    ) {
        Pageable pageable =
                buildPageable(
                        page,
                        size,
                        sort
                );

        PagedStudentResponse response =
                studentService.getStudents(
                        filter,
                        pageable
                );

        return noStore(
                ApiResponse.success(
                        "Students fetched successfully.",
                        response
                )
        );
    }

    // =====================================================================
    // STUDENT UPDATE
    // =====================================================================

    @PutMapping("/{studentId}")
    public ResponseEntity<ApiResponse<StudentProfileResponse>>
    updateStudent(
            @PathVariable
            @Positive(
                    message =
                            "Student ID must be greater than zero."
            )
            Long studentId,

            @Valid
            @RequestBody
            StudentUpdateRequest request
    ) {
        StudentProfileResponse response =
                studentService.updateStudent(
                        studentId,
                        request
                );

        return noStore(
                ApiResponse.success(
                        "Student updated successfully.",
                        response
                )
        );
    }

    // =====================================================================
    // ENROLLMENT UPDATE
    // =====================================================================

    @PutMapping("/{studentId}/enrollment")
    public ResponseEntity<ApiResponse<StudentEnrollmentResponse>>
    updateEnrollment(
            @PathVariable
            @Positive(
                    message =
                            "Student ID must be greater than zero."
            )
            Long studentId,

            @Valid
            @RequestBody
            StudentEnrollmentUpdateRequest request
    ) {
        StudentEnrollmentResponse response =
                studentService.updateEnrollment(
                        studentId,
                        request
                );

        return noStore(
                ApiResponse.success(
                        "Student enrollment updated successfully.",
                        response
                )
        );
    }

    // =====================================================================
    // STUDENT STATUS
    // =====================================================================

    @PutMapping("/{studentId}/status")
    public ResponseEntity<ApiResponse<StudentPersonalResponse>>
    changeStudentStatus(
            @PathVariable
            @Positive(
                    message =
                            "Student ID must be greater than zero."
            )
            Long studentId,

            @Valid
            @RequestBody
            StudentStatusChangeRequest request
    ) {
        StudentPersonalResponse response =
                studentService.changeStudentStatus(
                        studentId,
                        request
                );

        return noStore(
                ApiResponse.success(
                        "Student status updated successfully.",
                        response
                )
        );
    }

    // =====================================================================
    // STUDENT PHOTO
    // =====================================================================

    @PutMapping(
            value = "/{studentId}/photo",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ApiResponse<StudentPersonalResponse>>
    uploadOrReplacePhoto(
            @PathVariable
            @Positive(
                    message =
                            "Student ID must be greater than zero."
            )
            Long studentId,

            @RequestPart("photo")
            MultipartFile photo,

            @RequestParam
            @PositiveOrZero(
                    message =
                            "Student version cannot be negative."
            )
            Long version,

            @RequestParam
            @NotBlank(
                    message =
                            "Operation ID is required."
            )
            String operationId
    ) {
        StudentPhotoUploadRequest request =
                new StudentPhotoUploadRequest(
                        photo,
                        version,
                        operationId
                );

        StudentPersonalResponse response =
                photoService.uploadOrReplacePhoto(
                        studentId,
                        request
                );

        return noStore(
                ApiResponse.success(
                        "Student photo saved successfully.",
                        response
                )
        );
    }

    @GetMapping("/{studentId}/photo")
    public ResponseEntity<Resource>
    viewStudentPhoto(
            @PathVariable
            @Positive(
                    message =
                            "Student ID must be greater than zero."
            )
            Long studentId
    ) {
        StudentPhotoFile photo =
                photoService.loadStudentPhoto(
                        studentId
                );

        return serveInlineFile(
                photo.resource(),
                photo.fileName(),
                photo.contentType(),
                photo.fileSize()
        );
    }

    @DeleteMapping("/{studentId}/photo")
    public ResponseEntity<ApiResponse<StudentPersonalResponse>>
    removeStudentPhoto(
            @PathVariable
            @Positive(
                    message =
                            "Student ID must be greater than zero."
            )
            Long studentId,

            @RequestParam
            @PositiveOrZero(
                    message =
                            "Student version cannot be negative."
            )
            Long version
    ) {
        StudentPersonalResponse response =
                photoService.removeStudentPhoto(
                        studentId,
                        version
                );

        return noStore(
                ApiResponse.success(
                        "Student photo removed successfully.",
                        response
                )
        );
    }

    // =====================================================================
    // STUDENT DOCUMENTS
    // =====================================================================

    @PostMapping(
            value = "/{studentId}/documents",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ApiResponse<StudentDocumentResponse>>
    uploadDocument(
            @PathVariable
            @Positive(
                    message =
                            "Student ID must be greater than zero."
            )
            Long studentId,

            @Valid
            @RequestPart("metadata")
            StudentDocumentMetadataRequest metadata,

            @RequestPart("file")
            MultipartFile file
    ) {
        StudentDocumentUploadRequest request =
                new StudentDocumentUploadRequest(
                        metadata,
                        file
                );

        StudentDocumentResponse response =
                documentService.uploadDocument(
                        studentId,
                        request
                );

        return noStore(
                ApiResponse.success(
                        "Student document uploaded successfully.",
                        response
                )
        );
    }

    @GetMapping("/{studentId}/documents")
    public ResponseEntity<ApiResponse<List<StudentDocumentResponse>>>
    getStudentDocuments(
            @PathVariable
            @Positive(
                    message =
                            "Student ID must be greater than zero."
            )
            Long studentId
    ) {
        List<StudentDocumentResponse> response =
                documentService.getStudentDocuments(
                        studentId
                );

        return noStore(
                ApiResponse.success(
                        "Student documents fetched successfully.",
                        response
                )
        );
    }

    @GetMapping(
            "/{studentId}/documents/{documentId}"
    )
    public ResponseEntity<ApiResponse<StudentDocumentResponse>>
    getDocument(
            @PathVariable
            @Positive(
                    message =
                            "Student ID must be greater than zero."
            )
            Long studentId,

            @PathVariable
            @Positive(
                    message =
                            "Document ID must be greater than zero."
            )
            Long documentId
    ) {
        StudentDocumentResponse response =
                documentService.getDocument(
                        studentId,
                        documentId
                );

        return noStore(
                ApiResponse.success(
                        "Student document details fetched successfully.",
                        response
                )
        );
    }

    @PutMapping(
            "/{studentId}/documents/{documentId}/verification"
    )
    public ResponseEntity<ApiResponse<StudentDocumentResponse>>
    verifyDocument(
            @PathVariable
            @Positive(
                    message =
                            "Student ID must be greater than zero."
            )
            Long studentId,

            @PathVariable
            @Positive(
                    message =
                            "Document ID must be greater than zero."
            )
            Long documentId,

            @Valid
            @RequestBody
            StudentDocumentVerificationRequest request
    ) {
        StudentDocumentResponse response =
                documentService.verifyDocument(
                        studentId,
                        documentId,
                        request
                );

        return noStore(
                ApiResponse.success(
                        "Student document verification updated successfully.",
                        response
                )
        );
    }

    @GetMapping(
            "/{studentId}/documents/{documentId}/download"
    )
    public ResponseEntity<Resource>
    downloadDocument(
            @PathVariable
            @Positive(
                    message =
                            "Student ID must be greater than zero."
            )
            Long studentId,

            @PathVariable
            @Positive(
                    message =
                            "Document ID must be greater than zero."
            )
            Long documentId
    ) {
        StudentDocumentFile file =
                documentService.loadDocumentFile(
                        studentId,
                        documentId
                );

        return serveAttachment(
                file.resource(),
                file.downloadName(),
                file.contentType(),
                file.fileSize()
        );
    }

    @DeleteMapping(
            "/{studentId}/documents/{documentId}"
    )
    public ResponseEntity<ApiResponse<Void>>
    deactivateDocument(
            @PathVariable
            @Positive(
                    message =
                            "Student ID must be greater than zero."
            )
            Long studentId,

            @PathVariable
            @Positive(
                    message =
                            "Document ID must be greater than zero."
            )
            Long documentId
    ) {
        documentService.deactivateDocument(
                studentId,
                documentId
        );

        return noStore(
                ApiResponse.success(
                        "Student document removed successfully.",
                        null
                )
        );
    }

    // =====================================================================
    // PAGINATION
    // =====================================================================

    private Pageable buildPageable(
            Integer page,
            Integer size,
            String sort
    ) {
        int safePage =
                page != null
                        ? Math.max(page, 0)
                        : 0;

        int safeSize =
                size != null
                        ? Math.clamp(
                        size,
                        1,
                        MAX_PAGE_SIZE
                )
                        : 20;

        return PageRequest.of(
                safePage,
                safeSize,
                parseSort(sort)
        );
    }

    private Sort parseSort(
            String sort
    ) {
        if (!StringUtils.hasText(sort)) {
            return Sort.by(
                    Sort.Direction.DESC,
                    "studentId"
            );
        }

        String[] parts =
                sort.trim()
                        .split(
                                ",",
                                2
                        );

        String property =
                parts[0].trim();

        if (!StringUtils.hasText(property)) {
            property = "studentId";
        }

        Sort.Direction direction =
                parts.length > 1
                        && "asc".equalsIgnoreCase(
                        parts[1].trim()
                )
                        ? Sort.Direction.ASC
                        : Sort.Direction.DESC;

        return Sort.by(
                direction,
                property
        );
    }

    // =====================================================================
    // RESPONSE SECURITY
    // =====================================================================

    private <T> ResponseEntity<ApiResponse<T>> noStore(
            ApiResponse<T> body
    ) {
        return ResponseEntity.ok()
                .cacheControl(NO_STORE)
                .header(
                        HttpHeaders.PRAGMA,
                        "no-cache"
                )
                .header(
                        HttpHeaders.EXPIRES,
                        "0"
                )
                .body(body);
    }

    private ResponseEntity<Resource> serveInlineFile(
            Resource resource,
            String fileName,
            String contentType,
            long contentLength
    ) {
        ContentDisposition contentDisposition =
                ContentDisposition.inline()
                        .filename(
                                safeFilename(fileName),
                                StandardCharsets.UTF_8
                        )
                        .build();

        return servePrivateFile(
                resource,
                contentDisposition,
                contentType,
                contentLength
        );
    }

    private ResponseEntity<Resource> serveAttachment(
            Resource resource,
            String fileName,
            String contentType,
            long contentLength
    ) {
        ContentDisposition contentDisposition =
                ContentDisposition.attachment()
                        .filename(
                                safeFilename(fileName),
                                StandardCharsets.UTF_8
                        )
                        .build();

        return servePrivateFile(
                resource,
                contentDisposition,
                contentType,
                contentLength
        );
    }

    private ResponseEntity<Resource> servePrivateFile(
            Resource resource,
            ContentDisposition contentDisposition,
            String contentType,
            long contentLength
    ) {
        return ResponseEntity.ok()
                .cacheControl(NO_STORE)
                .header(
                        HttpHeaders.PRAGMA,
                        "no-cache"
                )
                .header(
                        HttpHeaders.EXPIRES,
                        "0"
                )
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        contentDisposition.toString()
                )
                .header(
                        "X-Content-Type-Options",
                        "nosniff"
                )
                .header(
                        "Content-Security-Policy",
                        "default-src 'none'; "
                                + "img-src 'self' data:; "
                                + "style-src 'unsafe-inline'; "
                                + "sandbox"
                )
                .contentType(
                        parseMediaType(contentType)
                )
                .contentLength(
                        contentLength
                )
                .body(resource);
    }

    private MediaType parseMediaType(
            String contentType
    ) {
        if (!StringUtils.hasText(contentType)) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }

        try {
            return MediaType.parseMediaType(
                    contentType.trim()
                            .toLowerCase(Locale.ROOT)
            );
        } catch (IllegalArgumentException exception) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }

    private String safeFilename(
            String fileName
    ) {
        if (!StringUtils.hasText(fileName)) {
            return "student-file";
        }

        String sanitized =
                fileName
                        .replace('\r', '_')
                        .replace('\n', '_')
                        .replace('/', '_')
                        .replace('\\', '_')
                        .trim();

        return sanitized.isBlank()
                ? "student-file"
                : sanitized;
    }
}