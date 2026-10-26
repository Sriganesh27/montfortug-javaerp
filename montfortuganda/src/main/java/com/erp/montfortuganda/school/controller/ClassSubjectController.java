package com.erp.montfortuganda.school.controller;

import com.erp.montfortuganda.common.response.ApiResponse;
import com.erp.montfortuganda.school.dto.ClassSubjectRequest;
import com.erp.montfortuganda.school.dto.ClassSubjectResponse;
import com.erp.montfortuganda.school.service.ClassSubjectService;
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
@RequestMapping("/api/class-subjects")
@PreAuthorize("hasRole('BRANCH_ADMIN')")
public class ClassSubjectController {

    private static final CacheControl NO_STORE = CacheControl.noStore().mustRevalidate();
    private final ClassSubjectService classSubjectService;

    public ClassSubjectController(ClassSubjectService classSubjectService) {
        this.classSubjectService = classSubjectService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ClassSubjectResponse>>> list(
            @RequestParam @Positive(message = "Academic year ID must be greater than zero.") Long academicYearId) {
        return noStore(ApiResponse.success("Class subject configurations fetched successfully.",
                classSubjectService.list(academicYearId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ClassSubjectResponse>> create(
            @Valid @RequestBody ClassSubjectRequest request) {
        return noStore(ApiResponse.success("Class subject configuration created successfully.",
                classSubjectService.create(request)));
    }

    @PutMapping("/{classSubjectId:\\d+}")
    public ResponseEntity<ApiResponse<ClassSubjectResponse>> update(
            @PathVariable @Positive(message = "Configuration ID must be greater than zero.") Long classSubjectId,
            @Valid @RequestBody ClassSubjectRequest request) {
        return noStore(ApiResponse.success("Class subject configuration updated successfully.",
                classSubjectService.update(classSubjectId, request)));
    }

    @PatchMapping("/{classSubjectId:\\d+}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivate(
            @PathVariable @Positive(message = "Configuration ID must be greater than zero.") Long classSubjectId) {
        classSubjectService.deactivate(classSubjectId);
        return noStore(ApiResponse.success("Class subject configuration deactivated successfully.", null));
    }

    private <T> ResponseEntity<T> noStore(T body) {
        return ResponseEntity.ok().cacheControl(NO_STORE).body(body);
    }
}
