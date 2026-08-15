package com.erp.montfortuganda.school.controller;

import com.erp.montfortuganda.common.response.ApiResponse;
import com.erp.montfortuganda.school.dto.SubjectRequestDTO;
import com.erp.montfortuganda.school.dto.SubjectResponseDTO;
import com.erp.montfortuganda.school.service.SubjectService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Branch-protected endpoints for Subject administration.
 *
 * <p>The authenticated user's branch is resolved exclusively in the service
 * layer. No branch ID is accepted from the browser.</p>
 */
@Validated
@RestController
@RequestMapping("/api/subjects")
@PreAuthorize("hasRole('BRANCH_ADMIN')")
public class SubjectController {

    private static final CacheControl NO_STORE =
            CacheControl.noStore()
                    .mustRevalidate();

    private final SubjectService subjectService;

    public SubjectController(
            SubjectService subjectService
    ) {
        this.subjectService = subjectService;
    }

    /**
     * Creates a Subject for the authenticated branch.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<SubjectResponseDTO>>
    createSubject(
            @Valid
            @RequestBody
            SubjectRequestDTO request
    ) {
        SubjectResponseDTO response =
                subjectService.createSubject(
                        request
                );

        return noStore(
                ApiResponse.success(
                        "Subject created successfully.",
                        response
                )
        );
    }

    /**
     * Updates a Subject owned by the authenticated branch.
     */
    @PutMapping("/{subjectId:\\d+}")
    public ResponseEntity<ApiResponse<SubjectResponseDTO>>
    updateSubject(
            @PathVariable
            @Positive(
                    message =
                            "Subject ID must be greater than zero."
            )
            Long subjectId,

            @Valid
            @RequestBody
            SubjectRequestDTO request
    ) {
        SubjectResponseDTO response =
                subjectService.updateSubject(
                        subjectId,
                        request
                );

        return noStore(
                ApiResponse.success(
                        "Subject updated successfully.",
                        response
                )
        );
    }

    /**
     * Returns one Subject owned by the authenticated branch.
     */
    @GetMapping("/{subjectId:\\d+}")
    public ResponseEntity<ApiResponse<SubjectResponseDTO>>
    getSubject(
            @PathVariable
            @Positive(
                    message =
                            "Subject ID must be greater than zero."
            )
            Long subjectId
    ) {
        SubjectResponseDTO response =
                subjectService.getSubject(
                        subjectId
                );

        return noStore(
                ApiResponse.success(
                        "Subject fetched successfully.",
                        response
                )
        );
    }

    /**
     * Lists all non-deleted Subjects of the authenticated branch.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<SubjectResponseDTO>>>
    getSubjects() {
        List<SubjectResponseDTO> response =
                subjectService.getSubjects();

        return noStore(
                ApiResponse.success(
                        "Subjects fetched successfully.",
                        response
                )
        );
    }

    /**
     * Lists only ACTIVE Subjects of the authenticated branch.
     *
     * <p>This endpoint is intended for reusable dropdowns such as Entrance
     * Test mark entry.</p>
     */
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<SubjectResponseDTO>>>
    getActiveSubjects() {
        List<SubjectResponseDTO> response =
                subjectService.getActiveSubjects();

        return noStore(
                ApiResponse.success(
                        "Active Subjects fetched successfully.",
                        response
                )
        );
    }

    /**
     * Activates or deactivates a Subject owned by the authenticated branch.
     */
    @PatchMapping("/{subjectId:\\d+}/active-status")
    public ResponseEntity<ApiResponse<SubjectResponseDTO>>
    changeSubjectActiveStatus(
            @PathVariable
            @Positive(
                    message =
                            "Subject ID must be greater than zero."
            )
            Long subjectId,

            @RequestParam
            @NotNull(
                    message =
                            "Active status is required."
            )
            Boolean active
    ) {
        SubjectResponseDTO response =
                subjectService
                        .changeSubjectActiveStatus(
                                subjectId,
                                active
                        );

        return noStore(
                ApiResponse.success(
                        active
                                ? "Subject activated successfully."
                                : "Subject deactivated successfully.",
                        response
                )
        );
    }

    private <T> ResponseEntity<ApiResponse<T>> noStore(
            ApiResponse<T> body
    ) {
        return ResponseEntity.ok()
                .cacheControl(
                        NO_STORE
                )
                .body(
                        body
                );
    }
}
