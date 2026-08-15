package com.erp.montfortuganda.school.dto;

import com.erp.montfortuganda.school.entity.ErpSubject;

public record SubjectResponseDTO(
        Long subjectId,
        Integer branchId,
        String subjectCode,
        String subjectName,
        String subjectShortName,
        ErpSubject.SubjectType subjectType,
        Boolean practical,
        Integer displayOrder,
        String description,
        ErpSubject.Status status,
        Boolean active
) {
}
