package com.erp.montfortuganda.school.dto;

import com.erp.montfortuganda.school.entity.Branch;
import com.erp.montfortuganda.school.entity.ErpAcademicTerm;
import com.erp.montfortuganda.school.entity.ErpAcademicYear;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Safe API response for an Academic Term.
 *
 * <p>The response includes the parent Academic Year and branch ownership
 * required by the UI, but does not expose JPA relationships directly.</p>
 */
public record AcademicTermResponse(

        Long termId,

        Long academicYearId,

        String academicYearCode,

        String academicYearName,

        Integer branchId,

        String branchName,

        String termCode,

        String termName,

        LocalDate startDate,

        LocalDate endDate,

        Integer displayOrder,

        ErpAcademicTerm.Status status,

        Boolean currentTerm,

        String description,

        Boolean active,

        Long version,

        Long createdBy,

        LocalDateTime createdAt,

        Long updatedBy,

        LocalDateTime updatedAt
) {

    /**
     * Converts an Academic Term entity into a response without returning lazy
     * entity relationships.
     */
    public static AcademicTermResponse fromEntity(
            ErpAcademicTerm academicTerm
    ) {
        if (academicTerm == null) {
            throw new IllegalArgumentException(
                    "Academic Term entity is required."
            );
        }

        ErpAcademicYear academicYear =
                academicTerm.getAcademicYear();

        Branch branch =
                academicYear == null
                        ? null
                        : academicYear.getBranch();

        return new AcademicTermResponse(
                academicTerm.getTermId(),
                academicYear == null
                        ? null
                        : academicYear.getAcademicYearId(),
                academicYear == null
                        ? null
                        : academicYear.getAcademicYearCode(),
                academicYear == null
                        ? null
                        : academicYear.getAcademicYearName(),
                branch == null
                        ? null
                        : branch.getBranchId(),
                branch == null
                        ? null
                        : branch.getBranchName(),
                academicTerm.getTermCode(),
                academicTerm.getTermName(),
                academicTerm.getStartDate(),
                academicTerm.getEndDate(),
                academicTerm.getDisplayOrder(),
                academicTerm.getStatus(),
                academicTerm.getCurrentTerm(),
                academicTerm.getDescription(),
                academicTerm.getActive(),
                academicTerm.getVersion(),
                academicTerm.getCreatedBy(),
                academicTerm.getCreatedAt(),
                academicTerm.getUpdatedBy(),
                academicTerm.getUpdatedAt()
        );
    }
}