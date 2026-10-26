package com.erp.montfortuganda.student.controller;

import com.erp.montfortuganda.common.response.ApiResponse;
import com.erp.montfortuganda.student.dto.request.StudentSubjectSelectionRequest;
import com.erp.montfortuganda.student.dto.response.StudentSubjectSelectionResponse;
import com.erp.montfortuganda.student.service.StudentSubjectService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/student-subjects")
@PreAuthorize("hasRole('BRANCH_ADMIN')")
public class StudentSubjectController {

    private static final CacheControl NO_STORE = CacheControl.noStore().mustRevalidate();
    private final StudentSubjectService studentSubjectService;

    public StudentSubjectController(StudentSubjectService studentSubjectService) {
        this.studentSubjectService = studentSubjectService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<StudentSubjectSelectionResponse>> getSelection(
            @RequestParam @Positive(message = "Enrollment ID must be greater than zero.") Long enrollmentId) {
        return noStore(ApiResponse.success("Student subject options fetched successfully.",
                studentSubjectService.getSelection(enrollmentId)));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<StudentSubjectSelectionResponse>> saveSelection(
            @RequestParam @Positive(message = "Enrollment ID must be greater than zero.") Long enrollmentId,
            @Valid @RequestBody StudentSubjectSelectionRequest request) {
        return noStore(ApiResponse.success("Student optional subjects saved successfully.",
                studentSubjectService.saveSelection(enrollmentId, request)));
    }

    private <T> ResponseEntity<T> noStore(T body) {
        return ResponseEntity.ok().cacheControl(NO_STORE).body(body);
    }
}
