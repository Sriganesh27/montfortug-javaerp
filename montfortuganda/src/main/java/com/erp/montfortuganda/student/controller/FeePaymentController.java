package com.erp.montfortuganda.student.controller;

import com.erp.montfortuganda.dto.ApiResponse;
import com.erp.montfortuganda.student.dto.request.FeePaymentCreateRequest;
import com.erp.montfortuganda.student.dto.response.FeePaymentCreateResponse;
import com.erp.montfortuganda.student.service.FeePaymentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Branch-admin API for recording payments against existing student fee
 * assignments. Branch ownership and payment rules are enforced by the service.
 */
@Validated
@RestController
@RequestMapping("/api/branchadmin/fees/assignments")
@PreAuthorize("hasRole('BRANCH_ADMIN')")
@RequiredArgsConstructor
public class FeePaymentController {

    private static final CacheControl NO_STORE =
            CacheControl.noStore().mustRevalidate();

    private final FeePaymentService feePaymentService;

    /**
     * Records a payment against one existing fee assignment.
     *
     * POST /api/branchadmin/fees/assignments/{feeAssignmentId}/payments
     */
    @PostMapping("/{feeAssignmentId:\\d+}/payments")
    public ResponseEntity<ApiResponse<FeePaymentCreateResponse>> recordPayment(
            @PathVariable
            @Positive(message = "Fee assignment ID must be greater than zero.")
            Long feeAssignmentId,
            @Valid
            @RequestBody
            FeePaymentCreateRequest request
    ) {
        FeePaymentCreateResponse response =
                feePaymentService.recordPayment(
                        feeAssignmentId,
                        request
                );

        return ResponseEntity.status(HttpStatus.CREATED)
                .cacheControl(NO_STORE)
                .body(
                        ApiResponse.success(
                                "Payment recorded successfully.",
                                response
                        )
                );
    }
}
