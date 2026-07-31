package com.erp.montfortuganda.school.controller;

import com.erp.montfortuganda.common.response.ApiResponse;
import com.erp.montfortuganda.school.dto.SectionRequest;
import com.erp.montfortuganda.school.dto.SectionResponse;
import com.erp.montfortuganda.school.entity.ErpSection;
import com.erp.montfortuganda.school.service.SectionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
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
 * Branch-protected endpoints for Section administration.
 *
 * <p>The authenticated user's branch is resolved in the service layer.
 * This controller intentionally exposes no branch ID parameter.</p>
 */
@Validated
@RestController
@RequestMapping("/api/sections")
@PreAuthorize("hasRole('BRANCH_ADMIN')")
public class SectionController {

    private static final CacheControl NO_STORE =
            CacheControl.noStore()
                    .mustRevalidate();

    private final SectionService sectionService;

    public SectionController(
            SectionService sectionService
    ) {
        this.sectionService = sectionService;
    }

    /**
     * Creates a Section for the authenticated branch.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<SectionResponse>>
    createSection(
            @Valid
            @RequestBody
            SectionRequest request
    ) {
        SectionResponse response =
                sectionService.createSection(
                        request
                );

        return noStore(
                ApiResponse.success(
                        "Section created successfully.",
                        response
                )
        );
    }

    /**
     * Updates a Section owned by the authenticated branch.
     */
    @PutMapping("/{sectionId:\\d+}")
    public ResponseEntity<ApiResponse<SectionResponse>>
    updateSection(
            @PathVariable
            @Positive(
                    message =
                            "Section ID must be greater than zero."
            )
            Long sectionId,

            @Valid
            @RequestBody
            SectionRequest request
    ) {
        SectionResponse response =
                sectionService.updateSection(
                        sectionId,
                        request
                );

        return noStore(
                ApiResponse.success(
                        "Section updated successfully.",
                        response
                )
        );
    }

    /**
     * Returns one Section owned by the authenticated branch.
     */
    @GetMapping("/{sectionId:\\d+}")
    public ResponseEntity<ApiResponse<SectionResponse>>
    getSection(
            @PathVariable
            @Positive(
                    message =
                            "Section ID must be greater than zero."
            )
            Long sectionId
    ) {
        SectionResponse response =
                sectionService.getSection(
                        sectionId
                );

        return noStore(
                ApiResponse.success(
                        "Section fetched successfully.",
                        response
                )
        );
    }

    /**
     * Lists Sections of the authenticated branch.
     *
     * <p>Academic Year and Class filters are applied only after branch
     * ownership has been verified by the service layer.</p>
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<SectionResponse>>>
    getSections(
            @RequestParam(
                    required = false
            )
            @Positive(
                    message =
                            "Academic Year ID must be greater than zero."
            )
            Long academicYearId,

            @RequestParam(
                    required = false
            )
            @Positive(
                    message =
                            "Class ID must be greater than zero."
            )
            Integer classId,

            @RequestParam(
                    required = false
            )
            ErpSection.Status status,

            @RequestParam(
                    required = false
            )
            Boolean active
    ) {
        if (
                classId != null
                        && academicYearId == null
        ) {
            throw new IllegalArgumentException(
                    "Academic Year ID is required when filtering by Class."
            );
        }

        List<SectionResponse> response;

        if (
                academicYearId != null
                        && classId != null
        ) {
            response =
                    Boolean.TRUE.equals(active)
                            ? sectionService
                            .getActiveSectionsByAcademicYearAndClass(
                                    academicYearId,
                                    classId
                            )
                            : sectionService
                            .getSectionsByAcademicYearAndClass(
                                    academicYearId,
                                    classId
                            );
        } else if (academicYearId != null) {
            response =
                    Boolean.TRUE.equals(active)
                            ? sectionService
                            .getActiveSectionsByAcademicYear(
                                    academicYearId
                            )
                            : sectionService
                            .getSectionsByAcademicYear(
                                    academicYearId
                            );
        } else if (status != null) {
            response =
                    sectionService
                            .getSectionsByStatus(
                                    status
                            );
        } else if (Boolean.TRUE.equals(active)) {
            response =
                    sectionService
                            .getActiveSections();
        } else {
            response =
                    sectionService
                            .getSections();
        }

        if (
                status != null
                        && (
                        academicYearId != null
                                || classId != null
                )
        ) {
            response =
                    response.stream()
                            .filter(
                                    section ->
                                            status.equals(
                                                    section.status()
                                            )
                            )
                            .toList();
        }

        if (Boolean.FALSE.equals(active)) {
            response =
                    response.stream()
                            .filter(
                                    section ->
                                            Boolean.FALSE.equals(
                                                    section.active()
                                            )
                            )
                            .toList();
        }

        return noStore(
                ApiResponse.success(
                        "Sections fetched successfully.",
                        response
                )
        );
    }

    /**
     * Activates or deactivates one Section.
     */
    @PatchMapping("/{sectionId:\\d+}/active-status")
    public ResponseEntity<ApiResponse<SectionResponse>>
    changeSectionActiveStatus(
            @PathVariable
            @Positive(
                    message =
                            "Section ID must be greater than zero."
            )
            Long sectionId,

            @RequestParam
            @NotNull(
                    message =
                            "Active status is required."
            )
            Boolean active,

            @RequestParam
            @NotNull(
                    message =
                            "Section version is required."
            )
            @PositiveOrZero(
                    message =
                            "Section version cannot be negative."
            )
            Long version
    ) {
        SectionResponse response =
                sectionService
                        .changeSectionActiveStatus(
                                sectionId,
                                active,
                                version
                        );

        return noStore(
                ApiResponse.success(
                        active
                                ? "Section activated successfully."
                                : "Section deactivated successfully.",
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