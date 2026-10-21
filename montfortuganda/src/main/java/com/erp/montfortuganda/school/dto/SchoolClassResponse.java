package com.erp.montfortuganda.school.dto;

import com.erp.montfortuganda.school.entity.SchoolClass;

/** API representation that does not expose JPA relationships. */
public record SchoolClassResponse(
        Integer classId,
        Integer branchId,
        Long academicYearId,
        String academicYearName,
        Integer levelId,
        String levelName,
        String classCode,
        String className,
        Integer displayOrder,
        Integer status
) {
    public static SchoolClassResponse fromEntity(SchoolClass schoolClass) {
        return new SchoolClassResponse(
                schoolClass.getClassId(),
                schoolClass.getBranch() == null ? null : schoolClass.getBranch().getBranchId(),
                schoolClass.getAcademicYear() == null ? null : schoolClass.getAcademicYear().getAcademicYearId(),
                schoolClass.getAcademicYear() == null ? null : schoolClass.getAcademicYear().getAcademicYearName(),
                schoolClass.getLevel() == null ? null : schoolClass.getLevel().getLevelId(),
                schoolClass.getLevel() == null ? null : schoolClass.getLevel().getLevelName(),
                schoolClass.getClassCode(),
                schoolClass.getClassName(),
                schoolClass.getDisplayOrder(),
                schoolClass.getStatus()
        );
    }
}
