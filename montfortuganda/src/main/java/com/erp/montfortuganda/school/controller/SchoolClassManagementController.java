package com.erp.montfortuganda.school.controller;

import com.erp.montfortuganda.common.response.ApiResponse;
import com.erp.montfortuganda.school.dto.SchoolClassRequest;
import com.erp.montfortuganda.school.dto.SchoolClassUpdateRequest;
import com.erp.montfortuganda.school.dto.SchoolClassResponse;
import com.erp.montfortuganda.school.service.SchoolClassManagementService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/classes")
@PreAuthorize("hasRole('BRANCH_ADMIN')")
public class SchoolClassManagementController {
    private static final CacheControl NO_STORE = CacheControl.noStore().mustRevalidate();
    private final SchoolClassManagementService service;

    public SchoolClassManagementController(SchoolClassManagementService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SchoolClassResponse>>> listClasses() {
        return noStore(ApiResponse.success("Classes fetched successfully.", service.listClasses()));
    }

    @GetMapping("/{classId:\\d+}")
    public ResponseEntity<ApiResponse<SchoolClassResponse>> getClass(
            @PathVariable @Positive(message = "Class ID must be greater than zero.") Integer classId) {
        return noStore(ApiResponse.success("Class fetched successfully.", service.getClass(classId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SchoolClassResponse>> createClass(@Valid @RequestBody SchoolClassRequest request) {
        return noStore(ApiResponse.success("Class created successfully.", service.createClass(request)));
    }

    @PutMapping("/{classId:\\d+}")
    public ResponseEntity<ApiResponse<SchoolClassResponse>> updateClass(
            @PathVariable @Positive(message = "Class ID must be greater than zero.") Integer classId,
            @Valid @RequestBody SchoolClassUpdateRequest request) {
        return noStore(ApiResponse.success("Class updated successfully.", service.updateClass(classId, request)));
    }

    private <T> ResponseEntity<T> noStore(T body) {
        return ResponseEntity.ok().cacheControl(NO_STORE).body(body);
    }
}
