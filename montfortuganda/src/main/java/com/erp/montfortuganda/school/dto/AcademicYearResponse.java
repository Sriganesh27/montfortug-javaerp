package com.erp.montfortuganda.school.dto;

import com.erp.montfortuganda.school.entity.Branch;
import com.erp.montfortuganda.school.entity.ErpAcademicYear;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Branch-safe Academic Year response.
 *
 * <p>This DTO exposes only the fields required by the UI and avoids returning
 * JPA relationships directly.</p>
 */
public record AcademicYearResponse(

        Long academicYearId,

        Integer branchId,

        String branchName,

        String academicYearCode,

        String academicYearName,

        LocalDate startDate,

        LocalDate endDate,

        LocalDate admissionStartDate,

        LocalDate admissionEndDate,

        ErpAcademicYear.Status status,

        Boolean currentYear,

        String description,

        Boolean active,

        Long version,

        Long createdBy,

        LocalDateTime createdAt,

        Long updatedBy,

        LocalDateTime updatedAt
) {

    /**
     * Converts an Academic Year entity into an API response without exposing
     * Sections or other lazy-loaded relationships.
     */
    public static AcademicYearResponse fromEntity(
            ErpAcademicYear academicYear
    ) {
        if (academicYear == null) {
            throw new IllegalArgumentException(
                    "Academic Year entity is required."
            );
        }

        Branch branch =
                academicYear.getBranch();

        return new AcademicYearResponse(
                academicYear.getAcademicYearId(),
                branch == null
                        ? null
                        : branch.getBranchId(),
                branch == null
                        ? null
                        : branch.getBranchName(),
                academicYear.getAcademicYearCode(),
                academicYear.getAcademicYearName(),
                academicYear.getStartDate(),
                academicYear.getEndDate(),
                academicYear.getAdmissionStartDate(),
                academicYear.getAdmissionEndDate(),
                academicYear.getStatus(),
                academicYear.getCurrentYear(),
                academicYear.getDescription(),
                academicYear.getActive(),
                academicYear.getVersion(),
                academicYear.getCreatedBy(),
                academicYear.getCreatedAt(),
                academicYear.getUpdatedBy(),
                academicYear.getUpdatedAt()
        );
    }
}