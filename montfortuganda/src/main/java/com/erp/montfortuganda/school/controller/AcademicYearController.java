package com.erp.montfortuganda.school.controller;

import com.erp.montfortuganda.common.response.ApiResponse;
import com.erp.montfortuganda.school.dto.AcademicYearRequest;
import com.erp.montfortuganda.school.dto.AcademicYearResponse;
import com.erp.montfortuganda.school.entity.ErpAcademicYear;
import com.erp.montfortuganda.school.service.AcademicYearService;
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
 * Branch-protected endpoints for Academic Year administration.
 *
 * <p>Branch ownership is resolved exclusively from the authenticated user.
 * This controller intentionally exposes no branch ID request parameter.</p>
 */
@Validated
@RestController
@RequestMapping("/api/academic-years")
@PreAuthorize("hasRole('BRANCH_ADMIN')")
public class AcademicYearController {

    private static final CacheControl NO_STORE =
            CacheControl.noStore()
                    .mustRevalidate();

    private final AcademicYearService academicYearService;

    public AcademicYearController(
            AcademicYearService academicYearService
    ) {
        this.academicYearService = academicYearService;
    }

    /**
     * Creates an Academic Year for the authenticated branch.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<AcademicYearResponse>>
    createAcademicYear(
            @Valid
            @RequestBody
            AcademicYearRequest request
    ) {
        AcademicYearResponse response =
                academicYearService.createAcademicYear(
                        request
                );

        return noStore(
                ApiResponse.success(
                        "Academic Year created successfully.",
                        response
                )
        );
    }

    /**
     * Updates an Academic Year owned by the authenticated branch.
     */
    @PutMapping("/{academicYearId:\\d+}")
    public ResponseEntity<ApiResponse<AcademicYearResponse>>
    updateAcademicYear(
            @PathVariable
            @Positive(
                    message =
                            "Academic Year ID must be greater than zero."
            )
            Long academicYearId,

            @Valid
            @RequestBody
            AcademicYearRequest request
    ) {
        AcademicYearResponse response =
                academicYearService.updateAcademicYear(
                        academicYearId,
                        request
                );

        return noStore(
                ApiResponse.success(
                        "Academic Year updated successfully.",
                        response
                )
        );
    }

    /**
     * Returns one Academic Year from the authenticated branch.
     */
    @GetMapping("/{academicYearId:\\d+}")
    public ResponseEntity<ApiResponse<AcademicYearResponse>>
    getAcademicYear(
            @PathVariable
            @Positive(
                    message =
                            "Academic Year ID must be greater than zero."
            )
            Long academicYearId
    ) {
        AcademicYearResponse response =
                academicYearService.getAcademicYear(
                        academicYearId
                );

        return noStore(
                ApiResponse.success(
                        "Academic Year fetched successfully.",
                        response
                )
        );
    }

    /**
     * Lists all Academic Years of the authenticated branch.
     *
     * <p>Optional filters are applied only after branch scoping.</p>
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<AcademicYearResponse>>>
    getAcademicYears(
            @RequestParam(
                    required = false
            )
            ErpAcademicYear.Status status,

            @RequestParam(
                    required = false
            )
            Boolean active
    ) {
        List<AcademicYearResponse> response;

        if (status != null) {
            response =
                    academicYearService
                            .getAcademicYearsByStatus(
                                    status
                            );

            if (active != null) {
                response =
                        response.stream()
                                .filter(
                                        academicYear ->
                                                active.equals(
                                                        academicYear.active()
                                                )
                                )
                                .toList();
            }
        } else if (Boolean.TRUE.equals(active)) {
            response =
                    academicYearService
                            .getActiveAcademicYears();
        } else {
            response =
                    academicYearService
                            .getAcademicYears();

            if (Boolean.FALSE.equals(active)) {
                response =
                        response.stream()
                                .filter(
                                        academicYear ->
                                                Boolean.FALSE.equals(
                                                        academicYear.active()
                                                )
                                )
                                .toList();
            }
        }

        return noStore(
                ApiResponse.success(
                        "Academic Years fetched successfully.",
                        response
                )
        );
    }

    /**
     * Returns the current active Academic Year of the authenticated branch.
     */
    @GetMapping("/current")
    public ResponseEntity<ApiResponse<AcademicYearResponse>>
    getCurrentAcademicYear() {
        AcademicYearResponse response =
                academicYearService.getCurrentAcademicYear();

        return noStore(
                ApiResponse.success(
                        "Current Academic Year fetched successfully.",
                        response
                )
        );
    }

    /**
     * Makes one Academic Year current for the authenticated branch.
     */
    @PatchMapping("/{academicYearId:\\d+}/current")
    public ResponseEntity<ApiResponse<AcademicYearResponse>>
    makeCurrentAcademicYear(
            @PathVariable
            @Positive(
                    message =
                            "Academic Year ID must be greater than zero."
            )
            Long academicYearId
    ) {
        AcademicYearResponse response =
                academicYearService
                        .makeCurrentAcademicYear(
                                academicYearId
                        );

        return noStore(
                ApiResponse.success(
                        "Current Academic Year updated successfully.",
                        response
                )
        );
    }

    /**
     * Activates or deactivates one Academic Year.
     */
    @PatchMapping("/{academicYearId:\\d+}/active-status")
    public ResponseEntity<ApiResponse<AcademicYearResponse>>
    changeAcademicYearActiveStatus(
            @PathVariable
            @Positive(
                    message =
                            "Academic Year ID must be greater than zero."
            )
            Long academicYearId,

            @RequestParam
            @NotNull(
                    message =
                            "Active status is required."
            )
            Boolean active,

            @RequestParam
            @NotNull(
                    message =
                            "Academic Year version is required."
            )
            @PositiveOrZero(
                    message =
                            "Academic Year version cannot be negative."
            )
            Long version
    ) {
        AcademicYearResponse response =
                academicYearService
                        .changeAcademicYearActiveStatus(
                                academicYearId,
                                active,
                                version
                        );

        return noStore(
                ApiResponse.success(
                        active
                                ? "Academic Year activated successfully."
                                : "Academic Year deactivated successfully.",
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