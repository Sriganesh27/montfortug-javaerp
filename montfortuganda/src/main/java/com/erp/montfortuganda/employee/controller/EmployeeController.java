package com.erp.montfortuganda.employee.controller;

import com.erp.montfortuganda.common.response.ApiResponse;
import com.erp.montfortuganda.employee.dto.request.EmployeeDeactivationRequest;
import com.erp.montfortuganda.employee.dto.request.EmployeeLoginAccountRequest;
import com.erp.montfortuganda.employee.dto.request.EmployeeTemporaryPasswordRequest;
import com.erp.montfortuganda.employee.dto.request.EmployeeRegistrationRequest;
import com.erp.montfortuganda.employee.dto.request.EmployeeSearchRequest;
import com.erp.montfortuganda.employee.dto.request.EmployeeUpdateRequest;
import com.erp.montfortuganda.employee.dto.response.EmployeeDetailResponse;
import com.erp.montfortuganda.employee.dto.response.EmployeeLoginRoleOptionResponse;
import com.erp.montfortuganda.employee.dto.response.EmployeeOptionResponse;
import com.erp.montfortuganda.employee.dto.response.EmployeePageResponse;
import com.erp.montfortuganda.employee.dto.response.EmployeeRegistrationResponse;
import com.erp.montfortuganda.employee.service.EmployeeService;
import com.erp.montfortuganda.employee.service.EmployeeService.EmployeePrivateFile;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.core.io.Resource;
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
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Branch Admin HTTP endpoints for the Employee module.
 *
 * <p>All business lookups remain branch-scoped inside the service layer.
 * Employee PII and private files are returned with no-store cache headers.</p>
 */
@SuppressWarnings("unused")
@Validated
@RestController
@RequestMapping("/api/branchadmin/employees")
@PreAuthorize("hasRole('BRANCH_ADMIN')")
public class EmployeeController {

    private static final CacheControl NO_STORE =
            CacheControl.noStore()
                    .mustRevalidate();

    private final EmployeeService employeeService;

    public EmployeeController(
            EmployeeService employeeService
    ) {
        this.employeeService = employeeService;
    }

    /**
     * Registers a new Employee with nested records and an optional login.
     */
    @PostMapping("/registrations")
    public ResponseEntity<ApiResponse<EmployeeRegistrationResponse>>
    registerEmployee(
            @Valid
            @RequestBody
            EmployeeRegistrationRequest request
    ) {
        EmployeeRegistrationResponse response =
                employeeService.registerEmployee(
                        request
                );

        return noStore(
                ApiResponse.success(
                        "Employee registered successfully.",
                        response
                )
        );
    }

    /**
     * Returns the temporary progress/result record owned by the authenticated
     * branch and user.
     */
    @GetMapping("/registrations/{operationId}/status")
    public ResponseEntity<ApiResponse<EmployeeRegistrationResponse>>
    getRegistrationStatus(
            @PathVariable
            @NotBlank(
                    message =
                            "Employee registration operation ID is required."
            )
            String operationId
    ) {
        EmployeeRegistrationResponse response =
                employeeService.getRegistrationStatus(
                        operationId
                );

        return noStore(
                ApiResponse.success(
                        "Employee registration status fetched successfully.",
                        response
                )
        );
    }

    /**
     * Searches Employees in the authenticated branch.
     */
    @PostMapping("/search")
    public ResponseEntity<ApiResponse<EmployeePageResponse>>
    searchEmployees(
            @Valid
            @RequestBody
            EmployeeSearchRequest request,

            @RequestParam(
                    defaultValue = "0"
            )
            Integer page,

            @RequestParam(
                    defaultValue = "10"
            )
            Integer size,

            @RequestParam(
                    defaultValue = "employeeId,desc"
            )
            String sort
    ) {
        EmployeePageResponse response =
                employeeService.searchEmployees(
                        request,
                        page,
                        size,
                        sort
                );

        return noStore(
                ApiResponse.success(
                        "Employees fetched successfully.",
                        response
                )
        );
    }

    /**
     * Returns active Employee-assignable login roles from the role master.
     */
    @GetMapping("/login-role-options")
    public ResponseEntity<ApiResponse<List<EmployeeLoginRoleOptionResponse>>>
    getLoginRoleOptions() {
        List<EmployeeLoginRoleOptionResponse> response =
                employeeService.getLoginRoleOptions();

        return noStore(
                ApiResponse.success(
                        "Employee login role options retrieved successfully.",
                        response
                )
        );
    }

    /**
     * Returns active branch-owned Employees eligible for Reporting Manager
     * selection. During edit, the current Employee can be excluded.
     */
    @GetMapping("/reporting-managers")
    public ResponseEntity<ApiResponse<List<EmployeeOptionResponse>>>
    getReportingManagers(
            @RequestParam(
                    required = false
            )
            @Positive(
                    message =
                            "Excluded Employee ID must be greater than zero."
            )
            Long excludeEmployeeId
    ) {
        List<EmployeeOptionResponse> response =
                employeeService.getReportingManagers(
                        excludeEmployeeId
                );

        return noStore(
                ApiResponse.success(
                        "Reporting managers fetched successfully.",
                        response
                )
        );
    }

    /**
     * Returns complete Employee details and nested records.
     */
    @GetMapping("/{employeeId}")
    public ResponseEntity<ApiResponse<EmployeeDetailResponse>>
    getEmployee(
            @PathVariable
            @Positive(
                    message =
                            "Employee ID must be greater than zero."
            )
            Long employeeId
    ) {
        EmployeeDetailResponse response =
                employeeService.getEmployee(
                        employeeId
                );

        return noStore(
                ApiResponse.success(
                        "Employee details fetched successfully.",
                        response
                )
        );
    }

    /**
     * Updates the Employee and synchronizes all nested collections.
     */
    @PutMapping("/{employeeId}")
    public ResponseEntity<ApiResponse<EmployeeDetailResponse>>
    updateEmployee(
            @PathVariable
            @Positive(
                    message =
                            "Employee ID must be greater than zero."
            )
            Long employeeId,

            @Valid
            @RequestBody
            EmployeeUpdateRequest request
    ) {
        EmployeeDetailResponse response =
                employeeService.updateEmployee(
                        employeeId,
                        request
                );

        return noStore(
                ApiResponse.success(
                        "Employee updated successfully.",
                        response
                )
        );
    }

    /**
     * Creates a login account for an existing Employee who was registered
     * without one.
     */
    @PostMapping("/{employeeId}/login-account")
    public ResponseEntity<ApiResponse<EmployeeDetailResponse>>
    createLoginAccount(
            @PathVariable
            @Positive(
                    message =
                            "Employee ID must be greater than zero."
            )
            Long employeeId,

            @Valid
            @RequestBody
            EmployeeLoginAccountRequest request
    ) {
        EmployeeDetailResponse response =
                employeeService.createLoginAccount(
                        employeeId,
                        request
                );

        return noStore(
                ApiResponse.success(
                        "Employee login account created successfully.",
                        response
                )
        );
    }

    /**
     * Generates a new temporary password and sends it to the Employee's
     * official email address.
     */
    @PostMapping("/{employeeId}/temporary-password")
    public ResponseEntity<ApiResponse<EmployeeDetailResponse>>
    resetAndSendTemporaryPassword(
            @PathVariable
            @Positive(
                    message =
                            "Employee ID must be greater than zero."
            )
            Long employeeId,

            @Valid
            @RequestBody
            EmployeeTemporaryPasswordRequest request
    ) {
        EmployeeDetailResponse response =
                employeeService.resetAndSendTemporaryPassword(
                        employeeId,
                        request
                );

        return noStore(
                ApiResponse.success(
                        "A new temporary password was generated and queued for email delivery.",
                        response
                )
        );
    }

    /**
     * Soft-deactivates the Employee and disables the linked login account.
     */
    @DeleteMapping("/{employeeId}")
    public ResponseEntity<ApiResponse<Void>>
    deactivateEmployee(
            @PathVariable
            @Positive(
                    message =
                            "Employee ID must be greater than zero."
            )
            Long employeeId,

            @Valid
            @RequestBody
            EmployeeDeactivationRequest request
    ) {
        employeeService.deactivateEmployee(
                employeeId,
                request
        );

        return noStore(
                ApiResponse.success(
                        "Employee deactivated successfully.",
                        null
                )
        );
    }

    /**
     * Securely displays the Employee profile photo.
     */
    @GetMapping("/{employeeId}/profile-photo")
    public ResponseEntity<Resource> viewProfilePhoto(
            @PathVariable
            @Positive(
                    message =
                            "Employee ID must be greater than zero."
            )
            Long employeeId
    ) {
        return servePrivateFile(
                employeeService.getProfilePhoto(
                        employeeId
                )
        );
    }

    /**
     * Securely displays the Employee signature.
     */
    @GetMapping("/{employeeId}/signature")
    public ResponseEntity<Resource> viewSignature(
            @PathVariable
            @Positive(
                    message =
                            "Employee ID must be greater than zero."
            )
            Long employeeId
    ) {
        return servePrivateFile(
                employeeService.getSignature(
                        employeeId
                )
        );
    }

    /**
     * Securely displays one qualification file.
     */
    @GetMapping(
            "/{employeeId}/qualifications/{qualificationId}/view"
    )
    public ResponseEntity<Resource> viewQualificationFile(
            @PathVariable
            @Positive(
                    message =
                            "Employee ID must be greater than zero."
            )
            Long employeeId,

            @PathVariable
            @Positive(
                    message =
                            "Qualification ID must be greater than zero."
            )
            Long qualificationId
    ) {
        return servePrivateFile(
                employeeService.getQualificationFile(
                        employeeId,
                        qualificationId
                )
        );
    }

    /**
     * Securely displays one experience certificate.
     */
    @GetMapping(
            "/{employeeId}/experiences/{experienceId}/certificate/view"
    )
    public ResponseEntity<Resource> viewExperienceCertificate(
            @PathVariable
            @Positive(
                    message =
                            "Employee ID must be greater than zero."
            )
            Long employeeId,

            @PathVariable
            @Positive(
                    message =
                            "Experience ID must be greater than zero."
            )
            Long experienceId
    ) {
        return servePrivateFile(
                employeeService.getExperienceCertificate(
                        employeeId,
                        experienceId
                )
        );
    }

    /**
     * Securely displays one relieving letter.
     */
    @GetMapping(
            "/{employeeId}/experiences/{experienceId}/relieving-letter/view"
    )
    public ResponseEntity<Resource> viewRelievingLetter(
            @PathVariable
            @Positive(
                    message =
                            "Employee ID must be greater than zero."
            )
            Long employeeId,

            @PathVariable
            @Positive(
                    message =
                            "Experience ID must be greater than zero."
            )
            Long experienceId
    ) {
        return servePrivateFile(
                employeeService.getRelievingLetter(
                        employeeId,
                        experienceId
                )
        );
    }

    /**
     * Securely displays one general Employee document.
     */
    @GetMapping(
            "/{employeeId}/documents/{documentId}/view"
    )
    public ResponseEntity<Resource> viewDocumentFile(
            @PathVariable
            @Positive(
                    message =
                            "Employee ID must be greater than zero."
            )
            Long employeeId,

            @PathVariable
            @Positive(
                    message =
                            "Document ID must be greater than zero."
            )
            Long documentId
    ) {
        return servePrivateFile(
                employeeService.getDocumentFile(
                        employeeId,
                        documentId
                )
        );
    }

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

    private ResponseEntity<Resource> servePrivateFile(
            EmployeePrivateFile file
    ) {
        MediaType mediaType =
                parseMediaType(
                        file.contentType()
                );

        ContentDisposition contentDisposition =
                ContentDisposition.inline()
                        .filename(
                                safeFilename(
                                        file.filename()
                                ),
                                StandardCharsets.UTF_8
                        )
                        .build();

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
                .contentType(mediaType)
                .contentLength(
                        file.contentLength()
                )
                .body(
                        file.resource()
                );
    }

    private MediaType parseMediaType(
            String contentType
    ) {
        if (!StringUtils.hasText(contentType)) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }

        try {
            return MediaType.parseMediaType(
                    contentType
            );
        } catch (IllegalArgumentException exception) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }

    private String safeFilename(
            String filename
    ) {
        if (!StringUtils.hasText(filename)) {
            return "employee-file";
        }

        String sanitized =
                filename
                        .replace('\r', '_')
                        .replace('\n', '_')
                        .replace('/', '_')
                        .replace('\\', '_')
                        .trim();

        return sanitized.isBlank()
                ? "employee-file"
                : sanitized;
    }
}
