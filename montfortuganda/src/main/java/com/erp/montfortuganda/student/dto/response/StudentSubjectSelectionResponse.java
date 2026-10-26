package com.erp.montfortuganda.student.dto.response;

import java.util.List;

public record StudentSubjectSelectionResponse(
        Long studentId,
        String admissionNo,
        String studentName,
        Long enrollmentId,
        Long academicYearId,
        Integer classId,
        String classCode,
        int requiredOptionalCount,
        List<StudentSubjectOptionResponse> subjects
) {}
