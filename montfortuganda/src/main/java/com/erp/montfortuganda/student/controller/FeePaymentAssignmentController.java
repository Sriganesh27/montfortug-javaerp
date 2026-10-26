package com.erp.montfortuganda.student.controller;

import com.erp.montfortuganda.dto.ApiResponse;
import com.erp.montfortuganda.student.dto.response.FeePaymentAssignmentResponse;
import com.erp.montfortuganda.student.service.FeePaymentAssignmentService;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Read-only payment support API for viewing fee assignments before recording
 * a payment. Branch scope is derived from the authenticated user's context.
 */
@Validated
@RestController
@RequestMapping("/api/branchadmin/payments/students")
@PreAuthorize("hasRole('BRANCH_ADMIN')")
@RequiredArgsConstructor
public class FeePaymentAssignmentController {

    private static final CacheControl NO_STORE =
            CacheControl.noStore().mustRevalidate();

    private final FeePaymentAssignmentService feePaymentAssignmentService;

    /**
     * GET /api/branchadmin/payments/students/{studentId}/fee-assignments
     */
    @GetMapping("/{studentId:\\d+}/fee-assignments")
    public ResponseEntity<ApiResponse<List<FeePaymentAssignmentResponse>>>
    getStudentFeeAssignments(
            @PathVariable @Positive Long studentId
    ) {
        return ResponseEntity.ok()
                .cacheControl(NO_STORE)
                .body(
                        ApiResponse.success(
                                "Student fee assignments fetched successfully.",
                                feePaymentAssignmentService
                                        .getStudentFeeAssignments(studentId)
                        )
                );
    }
}
