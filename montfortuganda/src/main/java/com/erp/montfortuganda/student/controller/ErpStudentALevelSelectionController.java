package com.erp.montfortuganda.student.controller;

import com.erp.montfortuganda.common.response.ApiResponse;
import com.erp.montfortuganda.student.dto.request.ErpStudentALevelSelectionRequest;
import com.erp.montfortuganda.student.dto.response.ErpStudentALevelSelectionResponse;
import com.erp.montfortuganda.student.service.ErpStudentALevelSelectionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Branch-protected endpoints for enrolled students' A' Level selections.
 */
@Validated
@RestController
@RequestMapping("/api/students")
@PreAuthorize("hasRole('BRANCH_ADMIN')")
public class ErpStudentALevelSelectionController {

    private static final CacheControl NO_STORE =
            CacheControl.noStore().mustRevalidate();

    private final ErpStudentALevelSelectionService selectionService;

    public ErpStudentALevelSelectionController(
            ErpStudentALevelSelectionService selectionService
    ) {
        this.selectionService = selectionService;
    }

    /** Assigns a combination or replaces the student's current combination. */
    @PutMapping("/{studentId:\\d+}/a-level-selection")
    public ResponseEntity<ApiResponse<ErpStudentALevelSelectionResponse>>
    assignOrChange(
            @PathVariable
            @Positive(message = "Student ID must be greater than zero.")
            Long studentId,

            @Valid
            @RequestBody
            ErpStudentALevelSelectionRequest request
    ) {
        ErpStudentALevelSelectionResponse response =
                selectionService.assignOrChange(studentId, request);

        return ResponseEntity.ok()
                .cacheControl(NO_STORE)
                .body(ApiResponse.success(
                        "A' Level combination assigned successfully.",
                        response
                ));
    }

    /** Returns the student's A' Level selection history for the current branch. */
    @GetMapping("/{studentId:\\d+}/a-level-selection/history")
    public ResponseEntity<ApiResponse<List<ErpStudentALevelSelectionResponse>>>
    getHistory(
            @PathVariable
            @Positive(message = "Student ID must be greater than zero.")
            Long studentId
    ) {
        List<ErpStudentALevelSelectionResponse> response =
                selectionService.getStudentHistory(studentId);

        return ResponseEntity.ok()
                .cacheControl(NO_STORE)
                .body(ApiResponse.success(
                        "A' Level selection history fetched successfully.",
                        response
                ));
    }
}
