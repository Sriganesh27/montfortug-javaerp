package com.erp.montfortuganda.fees.controller;

import com.erp.montfortuganda.dto.ApiResponse;
import com.erp.montfortuganda.fees.dto.*;
import com.erp.montfortuganda.fees.service.FeeStructureAssignmentService;
import com.erp.montfortuganda.fees.entity.ErpFeeStructure.StructureStatus;
import com.erp.montfortuganda.fees.service.FeeStructureService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Fee structure configuration API for the authenticated Branch Admin.
 * The API never accepts branch_id as a source of authorization.
 */
@Validated
@RestController
@RequestMapping("/api/branchadmin/fees/structures")
@PreAuthorize("hasRole('BRANCH_ADMIN')")
@RequiredArgsConstructor
public class FeeStructureController {

    private static final CacheControl NO_STORE =
            CacheControl.noStore().mustRevalidate();

    private final FeeStructureService feeStructureService;
    private final FeeStructureAssignmentService feeStructureAssignmentService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<FeeStructureResponse>>> list(
            @RequestParam(required = false) @Positive Long academicYearId,
            @RequestParam(required = false) @Positive Integer classId,
            @RequestParam(required = false) @Positive Long termId,
            @RequestParam(required = false) StructureStatus status
    ) {
        return noStore(ApiResponse.success(
                "Fee structures fetched successfully.",
                feeStructureService.list(academicYearId, classId, termId, status)
        ));
    }

    @GetMapping("/{feeStructureId:\\d+}")
    public ResponseEntity<ApiResponse<FeeStructureResponse>> get(
            @PathVariable @Positive Long feeStructureId
    ) {
        return noStore(ApiResponse.success(
                "Fee structure fetched successfully.",
                feeStructureService.get(feeStructureId)
        ));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<FeeStructureResponse>> create(
            @Valid @RequestBody FeeStructureCreateRequest request
    ) {
        return ResponseEntity.status(201).cacheControl(NO_STORE).body(
                ApiResponse.success(
                        "Draft fee structure created successfully.",
                        feeStructureService.create(request)
                )
        );
    }

    @PutMapping("/{feeStructureId:\\d+}")
    public ResponseEntity<ApiResponse<FeeStructureResponse>> update(
            @PathVariable @Positive Long feeStructureId,
            @Valid @RequestBody FeeStructureUpdateRequest request
    ) {
        return noStore(ApiResponse.success(
                "Draft fee structure updated successfully.",
                feeStructureService.update(feeStructureId, request)
        ));
    }

    @PostMapping("/{feeStructureId:\\d+}/items")
    public ResponseEntity<ApiResponse<FeeStructureItemResponse>> addItem(
            @PathVariable @Positive Long feeStructureId,
            @Valid @RequestBody FeeStructureItemRequest request
    ) {
        return ResponseEntity.status(201).cacheControl(NO_STORE).body(
                ApiResponse.success(
                        "Fee item added successfully.",
                        feeStructureService.addItem(feeStructureId, request)
                )
        );
    }

    @PutMapping("/{feeStructureId:\\d+}/items/{itemId:\\d+}")
    public ResponseEntity<ApiResponse<FeeStructureItemResponse>> updateItem(
            @PathVariable @Positive Long feeStructureId,
            @PathVariable @Positive Long itemId,
            @Valid @RequestBody FeeStructureItemRequest request
    ) {
        return noStore(ApiResponse.success(
                "Fee item updated successfully.",
                feeStructureService.updateItem(feeStructureId, itemId, request)
        ));
    }

    @DeleteMapping("/{feeStructureId:\\d+}/items/{itemId:\\d+}")
    public ResponseEntity<ApiResponse<FeeStructureItemResponse>> deactivateItem(
            @PathVariable @Positive Long feeStructureId,
            @PathVariable @Positive Long itemId
    ) {
        return noStore(ApiResponse.success(
                "Fee item deactivated successfully. Its record is retained for audit purposes.",
                feeStructureService.deactivateItem(feeStructureId, itemId)
        ));
    }

    @PostMapping("/{feeStructureId:\\d+}/assignments")
    public ResponseEntity<ApiResponse<FeeStructureAssignmentResponse>> assignToStudents(
            @PathVariable @Positive Long feeStructureId,
            @Valid @RequestBody FeeStructureAssignmentRequest request
    ) {
        return noStore(ApiResponse.success(
                "Fee structure assigned successfully.",
                feeStructureAssignmentService.assign(feeStructureId, request)
        ));
    }

    @PostMapping("/{feeStructureId:\\d+}/publish")
    public ResponseEntity<ApiResponse<FeeStructureResponse>> publish(
            @PathVariable @Positive Long feeStructureId
    ) {
        return noStore(ApiResponse.success(
                "Fee structure published successfully.",
                feeStructureService.publish(feeStructureId)
        ));
    }

    @PostMapping("/{feeStructureId:\\d+}/archive")
    public ResponseEntity<ApiResponse<FeeStructureResponse>> archive(
            @PathVariable @Positive Long feeStructureId
    ) {
        return noStore(ApiResponse.success(
                "Fee structure archived successfully.",
                feeStructureService.archive(feeStructureId)
        ));
    }

    private <T> ResponseEntity<T> noStore(T body) {
        return ResponseEntity.ok().cacheControl(NO_STORE).body(body);
    }
}
