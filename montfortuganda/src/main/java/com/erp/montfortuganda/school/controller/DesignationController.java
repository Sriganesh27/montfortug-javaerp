package com.erp.montfortuganda.school.controller;

import com.erp.montfortuganda.common.dto.ApiResponse;
import com.erp.montfortuganda.common.dto.PagedResponse;
import com.erp.montfortuganda.model.enums.RecordStatus;
import com.erp.montfortuganda.school.dto.DesignationDTO;
import com.erp.montfortuganda.school.service.DesignationDeletionService;
import com.erp.montfortuganda.school.service.DesignationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/designations")
@RequiredArgsConstructor
public class DesignationController {

    private final DesignationService designationService;
    private final DesignationDeletionService designationDeletionService;

    @PostMapping
    public ResponseEntity<ApiResponse<DesignationDTO>> createDesignation(@Valid @RequestBody DesignationDTO dto) {
        DesignationDTO created = designationService.createDesignation(dto);
        return ResponseEntity.ok(ApiResponse.success("Designation created successfully", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DesignationDTO>> updateDesignation(
            @PathVariable Long id, @Valid @RequestBody DesignationDTO dto) {
        DesignationDTO updated = designationService.updateDesignation(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Designation updated successfully", updated));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DesignationDTO>> getDesignationById(@PathVariable Long id) {
        DesignationDTO designation = designationService.getDesignationById(id);
        return ResponseEntity.ok(ApiResponse.success("Designation fetched successfully", designation));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<DesignationDTO>>> searchDesignations(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer branchId,
            @RequestParam(required = false) RecordStatus status,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<DesignationDTO> result = designationService.searchDesignations(
                keyword, branchId, status, active, pageable);

        return ResponseEntity.ok(ApiResponse.success("Designations fetched successfully", result));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDesignation(@PathVariable Long id) {
        designationDeletionService.softDelete(id);
        return ResponseEntity.ok(ApiResponse.success("Designation deleted successfully", null));
    }
}