package com.erp.montfortuganda.student.controller;

import com.erp.montfortuganda.dto.ApiResponse;
import com.erp.montfortuganda.student.dto.response.FeePaymentHistoryResponse;
import com.erp.montfortuganda.student.service.FeePaymentHistoryService;
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

@Validated
@RestController
@RequestMapping("/api/branchadmin/payments/students")
@PreAuthorize("hasRole('BRANCH_ADMIN')")
@RequiredArgsConstructor
public class FeePaymentHistoryController {

    private static final CacheControl NO_STORE =
            CacheControl.noStore().mustRevalidate();

    private final FeePaymentHistoryService feePaymentHistoryService;

    /**
     * GET /api/branchadmin/payments/students/{studentId}/history
     */
    @GetMapping("/{studentId:\\d+}/history")
    public ResponseEntity<ApiResponse<List<FeePaymentHistoryResponse>>>
    getStudentPaymentHistory(
            @PathVariable @Positive Long studentId
    ) {
        return ResponseEntity.ok()
                .cacheControl(NO_STORE)
                .body(
                        ApiResponse.success(
                                "Student payment history fetched successfully.",
                                feePaymentHistoryService
                                        .getStudentPaymentHistory(studentId)
                        )
                );
    }
}
