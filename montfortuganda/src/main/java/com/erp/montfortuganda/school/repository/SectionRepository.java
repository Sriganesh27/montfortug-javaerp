package com.erp.montfortuganda.school.repository;

import com.erp.montfortuganda.school.entity.ErpSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Branch-safe repository for Section records.
 *
 * <p>A Section is valid only when its own branch matches the branch that owns
 * its Academic Year. Service-layer validation should still enforce this rule
 * before every create or update operation.</p>
 */
@Repository
public interface SectionRepository
        extends JpaRepository<ErpSection, Long> {

    List<ErpSection>
    findAllByBranchBranchIdOrderBySchoolClassDisplayOrderAscSectionCodeAsc(
            Integer branchId
    );

    List<ErpSection>
    findAllByBranchBranchIdAndActiveTrueOrderBySchoolClassDisplayOrderAscSectionCodeAsc(
            Integer branchId
    );

    List<ErpSection>
    findAllByBranchBranchIdAndAcademicYearAcademicYearIdOrderBySchoolClassDisplayOrderAscSectionCodeAsc(
            Integer branchId,
            Long academicYearId
    );

    List<ErpSection>
    findAllByBranchBranchIdAndAcademicYearAcademicYearIdAndActiveTrueOrderBySchoolClassDisplayOrderAscSectionCodeAsc(
            Integer branchId,
            Long academicYearId
    );

    List<ErpSection>
    findAllByBranchBranchIdAndAcademicYearAcademicYearIdAndSchoolClassClassIdOrderBySectionCodeAsc(
            Integer branchId,
            Long academicYearId,
            Integer classId
    );

    List<ErpSection>
    findAllByBranchBranchIdAndAcademicYearAcademicYearIdAndSchoolClassClassIdAndActiveTrueOrderBySectionCodeAsc(
            Integer branchId,
            Long academicYearId,
            Integer classId
    );

    Optional<ErpSection>
    findBySectionIdAndBranchBranchId(
            Long sectionId,
            Integer branchId
    );

    Optional<ErpSection>
    findBySectionIdAndBranchBranchIdAndActiveTrue(
            Long sectionId,
            Integer branchId
    );

    Optional<ErpSection>
    findByBranchBranchIdAndAcademicYearAcademicYearIdAndSchoolClassClassIdAndSectionCodeIgnoreCase(
            Integer branchId,
            Long academicYearId,
            Integer classId,
            String sectionCode
    );

    Optional<ErpSection>
    findByBranchBranchIdAndAcademicYearAcademicYearIdAndSchoolClassClassIdAndSectionCodeIgnoreCaseAndActiveTrue(
            Integer branchId,
            Long academicYearId,
            Integer classId,
            String sectionCode
    );

    boolean existsByBranchBranchIdAndAcademicYearAcademicYearIdAndSchoolClassClassIdAndSectionCodeIgnoreCase(
            Integer branchId,
            Long academicYearId,
            Integer classId,
            String sectionCode
    );

    boolean existsByBranchBranchIdAndAcademicYearAcademicYearIdAndSchoolClassClassIdAndSectionCodeIgnoreCaseAndSectionIdNot(
            Integer branchId,
            Long academicYearId,
            Integer classId,
            String sectionCode,
            Long sectionId
    );

    /**
     * Returns only Sections whose own branch matches the branch that owns
     * their Academic Year.
     */
    @Query("""
            SELECT section
            FROM ErpSection section
            WHERE section.branch.branchId = :branchId
              AND section.academicYear.academicYearId = :academicYearId
              AND section.academicYear.branch.branchId = :branchId
              AND section.active = true
            ORDER BY
                section.schoolClass.displayOrder ASC,
                section.sectionCode ASC
            """)
    List<ErpSection> findAllConsistentActiveByBranchAndAcademicYear(
            @Param("branchId") Integer branchId,
            @Param("academicYearId") Long academicYearId
    );

    /**
     * Loads active Sections for one class only when Section branch and
     * Academic Year branch both match the authenticated branch.
     */
    @Query("""
            SELECT section
            FROM ErpSection section
            WHERE section.branch.branchId = :branchId
              AND section.academicYear.academicYearId = :academicYearId
              AND section.academicYear.branch.branchId = :branchId
              AND section.schoolClass.classId = :classId
              AND section.active = true
            ORDER BY section.sectionCode ASC
            """)
    List<ErpSection> findAllConsistentActiveByBranchYearAndClass(
            @Param("branchId") Integer branchId,
            @Param("academicYearId") Long academicYearId,
            @Param("classId") Integer classId
    );

    /**
     * Loads one active Section only when all branch, year and class ownership
     * values are consistent.
     */
    @Query("""
            SELECT section
            FROM ErpSection section
            WHERE section.sectionId = :sectionId
              AND section.branch.branchId = :branchId
              AND section.academicYear.academicYearId = :academicYearId
              AND section.academicYear.branch.branchId = :branchId
              AND section.schoolClass.classId = :classId
              AND section.active = true
            """)
    Optional<ErpSection> findConsistentActiveSection(
            @Param("sectionId") Long sectionId,
            @Param("branchId") Integer branchId,
            @Param("academicYearId") Long academicYearId,
            @Param("classId") Integer classId
    );

    /**
     * Finds Section/Academic Year branch mismatches. This is useful for
     * migration verification and administrative integrity checks.
     */
    @Query("""
            SELECT section
            FROM ErpSection section
            WHERE section.branch.branchId
                  <> section.academicYear.branch.branchId
            ORDER BY section.sectionId ASC
            """)
    List<ErpSection> findBranchOwnershipMismatches();
}