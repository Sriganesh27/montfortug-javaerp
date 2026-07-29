package com.erp.montfortuganda.student.dto.response;

import java.time.LocalDate;
import java.util.List;

/**
 * Branch-protected reference data used by Student registration,
 * enrollment and profile forms.
 *
 * Only active and authorized records should be added to this response
 * by the Student service.
 */
public record StudentReferenceDataResponse(

        List<AcademicYearOption> academicYears,

        List<AcademicTermOption> academicTerms,

        List<LevelOption> levels,

        List<ClassOption> classes,

        List<SectionOption> sections

) {

    public StudentReferenceDataResponse {
        academicYears = immutableList(
                academicYears
        );

        academicTerms = immutableList(
                academicTerms
        );

        levels = immutableList(
                levels
        );

        classes = immutableList(
                classes
        );

        sections = immutableList(
                sections
        );
    }

    private static <T> List<T> immutableList(
            List<T> values
    ) {
        return values == null
                ? List.of()
                : List.copyOf(values);
    }

    /**
     * Academic Year dropdown option.
     */
    public record AcademicYearOption(

            Long academicYearId,

            String academicYearCode,

            String academicYearName,

            LocalDate startDate,

            LocalDate endDate,

            String status,

            Boolean currentYear

    ) {
    }

    /**
     * Academic Term option belonging to an Academic Year.
     */
    public record AcademicTermOption(

            Long termId,

            Long academicYearId,

            String termCode,

            String termName,

            LocalDate startDate,

            LocalDate endDate,

            Integer displayOrder,

            String status,

            Boolean currentTerm

    ) {
    }

    /**
     * Education Level option used for previous-level selection.
     */
    public record LevelOption(

            Integer levelId,

            String levelName,

            Integer displayOrder

    ) {
    }

    /**
     * Class option linked to its education Level.
     */
    public record ClassOption(

            Integer classId,

            Integer levelId,

            String classCode,

            String className,

            Integer displayOrder

    ) {
    }

    /**
     * Section option restricted to the authenticated user's branch.
     */
    public record SectionOption(

            Long sectionId,

            Integer branchId,

            Long academicYearId,

            Integer classId,

            String sectionCode,

            String sectionName,

            Integer capacity

    ) {
    }
}