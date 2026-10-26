package com.erp.montfortuganda.student.dto.response;

public record StudentSubjectOptionResponse(
        Long subjectId,
        String subjectCode,
        String subjectName,
        String subjectType,
        String requirement,
        boolean selected,
        Long studentSubjectId
) {}
