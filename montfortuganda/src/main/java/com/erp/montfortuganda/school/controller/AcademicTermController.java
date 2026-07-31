package com.erp.montfortuganda.school.controller;

import com.erp.montfortuganda.common.response.ApiResponse;
import com.erp.montfortuganda.school.dto.AcademicTermRequest;
import com.erp.montfortuganda.school.dto.AcademicTermResponse;
import com.erp.montfortuganda.school.entity.ErpAcademicTerm;
import com.erp.montfortuganda.school.service.AcademicTermService;
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
 * Branch-protected endpoints for Academic Term administration.
 *
 * <p>Branch ownership is inherited through the parent Academic Year and is
 * always verified by the service layer. This controller intentionally exposes
 * no branch ID parameter.</p>
 */
@Validated
@RestController
@RequestMapping("/api/academic-terms")
@PreAuthorize("hasRole('BRANCH_ADMIN')")
public class AcademicTermController {

    private static final CacheControl NO_STORE =
            CacheControl.noStore()
                    .mustRevalidate();

    private final AcademicTermService academicTermService;

    public AcademicTermController(
            AcademicTermService academicTermService
    ) {
        this.academicTermService = academicTermService;
    }

    /**
     * Creates an Academic Term under an Academic Year owned by the
     * authenticated branch.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<AcademicTermResponse>>
    createAcademicTerm(
            @Valid
            @RequestBody
            AcademicTermRequest request
    ) {
        AcademicTermResponse response =
                academicTermService.createAcademicTerm(
                        request
                );

        return noStore(
                ApiResponse.success(
                        "Academic Term created successfully.",
                        response
                )
        );
    }

    /**
     * Updates an Academic Term visible to the authenticated branch.
     */
    @PutMapping("/{termId:\\d+}")
    public ResponseEntity<ApiResponse<AcademicTermResponse>>
    updateAcademicTerm(
            @PathVariable
            @Positive(
                    message =
                            "Academic Term ID must be greater than zero."
            )
            Long termId,

            @Valid
            @RequestBody
            AcademicTermRequest request
    ) {
        AcademicTermResponse response =
                academicTermService.updateAcademicTerm(
                        termId,
                        request
                );

        return noStore(
                ApiResponse.success(
                        "Academic Term updated successfully.",
                        response
                )
        );
    }

    /**
     * Returns one Academic Term visible to the authenticated branch.
     */
    @GetMapping("/{termId:\\d+}")
    public ResponseEntity<ApiResponse<AcademicTermResponse>>
    getAcademicTerm(
            @PathVariable
            @Positive(
                    message =
                            "Academic Term ID must be greater than zero."
            )
            Long termId
    ) {
        AcademicTermResponse response =
                academicTermService.getAcademicTerm(
                        termId
                );

        return noStore(
                ApiResponse.success(
                        "Academic Term fetched successfully.",
                        response
                )
        );
    }

    /**
     * Lists Terms under one Academic Year owned by the authenticated branch.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<AcademicTermResponse>>>
    getAcademicTerms(
            @RequestParam
            @Positive(
                    message =
                            "Academic Year ID must be greater than zero."
            )
            Long academicYearId,

            @RequestParam(
                    required = false
            )
            ErpAcademicTerm.Status status,

            @RequestParam(
                    required = false
            )
            Boolean active
    ) {
        List<AcademicTermResponse> response;

        if (status != null) {
            response =
                    academicTermService
                            .getAcademicTermsByStatus(
                                    academicYearId,
                                    status
                            );

            if (active != null) {
                response =
                        response.stream()
                                .filter(
                                        term ->
                                                active.equals(
                                                        term.active()
                                                )
                                )
                                .toList();
            }
        } else if (Boolean.TRUE.equals(active)) {
            response =
                    academicTermService
                            .getActiveAcademicTerms(
                                    academicYearId
                            );
        } else {
            response =
                    academicTermService
                            .getAcademicTerms(
                                    academicYearId
                            );

            if (Boolean.FALSE.equals(active)) {
                response =
                        response.stream()
                                .filter(
                                        term ->
                                                Boolean.FALSE.equals(
                                                        term.active()
                                                )
                                )
                                .toList();
            }
        }

        return noStore(
                ApiResponse.success(
                        "Academic Terms fetched successfully.",
                        response
                )
        );
    }

    /**
     * Returns the current active Term for the authenticated branch's current
     * Academic Year.
     */
    @GetMapping("/current")
    public ResponseEntity<ApiResponse<AcademicTermResponse>>
    getCurrentAcademicTerm() {
        AcademicTermResponse response =
                academicTermService.getCurrentAcademicTerm();

        return noStore(
                ApiResponse.success(
                        "Current Academic Term fetched successfully.",
                        response
                )
        );
    }

    /**
     * Makes one Term current inside its Academic Year.
     */
    @PatchMapping("/{termId:\\d+}/current")
    public ResponseEntity<ApiResponse<AcademicTermResponse>>
    makeCurrentAcademicTerm(
            @PathVariable
            @Positive(
                    message =
                            "Academic Term ID must be greater than zero."
            )
            Long termId
    ) {
        AcademicTermResponse response =
                academicTermService
                        .makeCurrentAcademicTerm(
                                termId
                        );

        return noStore(
                ApiResponse.success(
                        "Current Academic Term updated successfully.",
                        response
                )
        );
    }

    /**
     * Activates or deactivates one Academic Term.
     */
    @PatchMapping("/{termId:\\d+}/active-status")
    public ResponseEntity<ApiResponse<AcademicTermResponse>>
    changeAcademicTermActiveStatus(
            @PathVariable
            @Positive(
                    message =
                            "Academic Term ID must be greater than zero."
            )
            Long termId,

            @RequestParam
            @NotNull(
                    message =
                            "Active status is required."
            )
            Boolean active,

            @RequestParam
            @NotNull(
                    message =
                            "Academic Term version is required."
            )
            @PositiveOrZero(
                    message =
                            "Academic Term version cannot be negative."
            )
            Long version
    ) {
        AcademicTermResponse response =
                academicTermService
                        .changeAcademicTermActiveStatus(
                                termId,
                                active,
                                version
                        );

        return noStore(
                ApiResponse.success(
                        active
                                ? "Academic Term activated successfully."
                                : "Academic Term deactivated successfully.",
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