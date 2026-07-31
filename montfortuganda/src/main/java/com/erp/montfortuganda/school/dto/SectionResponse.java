package com.erp.montfortuganda.school.dto;

import com.erp.montfortuganda.school.entity.Branch;
import com.erp.montfortuganda.school.entity.ErpAcademicYear;
import com.erp.montfortuganda.school.entity.ErpSection;
import com.erp.montfortuganda.school.entity.SchoolClass;

import java.time.LocalDateTime;

/**
 * Safe API response for a branch-owned Section.
 *
 * <p>The response exposes the branch, Academic Year and Class values required
 * by the UI without returning lazy JPA relationships directly.</p>
 */
public record SectionResponse(

        Long sectionId,

        Integer branchId,

        String branchName,

        String schoolCode,

        Long academicYearId,

        String academicYearCode,

        String academicYearName,

        Integer classId,

        String classCode,

        String className,

        Integer classDisplayOrder,

        String sectionCode,

        String sectionName,

        Integer capacity,

        String description,

        ErpSection.Status status,

        Boolean active,

        Long version,

        Long createdBy,

        LocalDateTime createdAt,

        Long updatedBy,

        LocalDateTime updatedAt
) {

    /**
     * Converts a Section entity into an API response without exposing entity
     * relationships or recursive collections.
     */
    public static SectionResponse fromEntity(
            ErpSection section
    ) {
        if (section == null) {
            throw new IllegalArgumentException(
                    "Section entity is required."
            );
        }

        Branch branch =
                section.getBranch();

        ErpAcademicYear academicYear =
                section.getAcademicYear();

        SchoolClass schoolClass =
                section.getSchoolClass();

        return new SectionResponse(
                section.getSectionId(),
                branch == null
                        ? null
                        : branch.getBranchId(),
                branch == null
                        ? null
                        : branch.getBranchName(),
                branch == null
                        ? null
                        : branch.getSchoolCode(),
                academicYear == null
                        ? null
                        : academicYear.getAcademicYearId(),
                academicYear == null
                        ? null
                        : academicYear.getAcademicYearCode(),
                academicYear == null
                        ? null
                        : academicYear.getAcademicYearName(),
                schoolClass == null
                        ? null
                        : schoolClass.getClassId(),
                schoolClass == null
                        ? null
                        : schoolClass.getClassCode(),
                schoolClass == null
                        ? null
                        : schoolClass.getClassName(),
                schoolClass == null
                        ? null
                        : schoolClass.getDisplayOrder(),
                section.getSectionCode(),
                section.getSectionName(),
                section.getCapacity(),
                section.getDescription(),
                section.getStatus(),
                section.getActive(),
                section.getVersion(),
                section.getCreatedBy(),
                section.getCreatedAt(),
                section.getUpdatedBy(),
                section.getUpdatedAt()
        );
    }
}