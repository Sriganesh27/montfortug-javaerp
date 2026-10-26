package com.erp.montfortuganda.school.dto;

import com.erp.montfortuganda.school.entity.ErpClassSubject;

public record ClassSubjectResponse(
        Long classSubjectId,
        Long academicYearId,
        String academicYearName,
        Integer levelId,
        String levelName,
        Long subjectId,
        String subjectCode,
        String subjectName,
        Integer startClassId,
        String startClassCode,
        Integer endClassId,
        String endClassCode,
        ErpClassSubject.Requirement subjectRequirement,
        Long version
) {}
