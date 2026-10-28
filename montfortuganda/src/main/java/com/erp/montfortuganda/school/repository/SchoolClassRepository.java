package com.erp.montfortuganda.school.repository;

import com.erp.montfortuganda.school.entity.SchoolClass;
import com.erp.montfortuganda.school.entity.ErpAcademicYear;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SchoolClassRepository extends JpaRepository<SchoolClass, Integer> {

    // Required by SchoolClassManagementService. These branch-scoped methods
    // preserve the existing class-management API while year eligibility is added.
    List<SchoolClass> findAllByBranch_BranchIdOrderByDisplayOrderAscClassIdAsc(Integer branchId);

    java.util.Optional<SchoolClass> findTopByBranch_BranchIdOrderByDisplayOrderDescClassIdDesc(Integer branchId);

    boolean existsByBranch_BranchIdAndClassCodeIgnoreCase(Integer branchId, String classCode);

    boolean existsByBranch_BranchIdAndClassCodeIgnoreCaseAndClassIdNot(
            Integer branchId, String classCode, Integer classId);

    java.util.Optional<SchoolClass> findByClassIdAndBranch_BranchId(Integer classId, Integer branchId);

    /** Legacy lookup retained while dependent modules are migrated. */
    List<SchoolClass> findByStatusOrderByDisplayOrderAsc(Integer status);

    /** Branch/year-scoped classes; available once the class migration is backfilled. */
    List<SchoolClass> findByBranch_BranchIdAndAcademicYear_AcademicYearIdAndStatusOrderByDisplayOrderAsc(
            Integer branchId, Long academicYearId, Integer status);

    /** Used to validate that a selected class belongs to the requested branch/year. */
    boolean existsByClassIdAndBranch_BranchIdAndAcademicYear_AcademicYearIdAndStatus(
            Integer classId, Integer branchId, Long academicYearId, Integer status);

    boolean existsByBranch_BranchIdAndAcademicYear_AcademicYearIdAndClassCodeIgnoreCase(
            Integer branchId, Long academicYearId, String classCode);

    boolean existsByBranch_BranchIdAndAcademicYear_AcademicYearIdAndClassCodeIgnoreCaseAndClassIdNot(
            Integer branchId, Long academicYearId, String classCode, Integer classId);

    java.util.Optional<SchoolClass> findByClassIdAndBranch_BranchIdAndAcademicYear_AcademicYearId(
            Integer classId, Integer branchId, Long academicYearId);

    List<SchoolClass> findAllByBranch_BranchIdAndAcademicYear_AcademicYearIdOrderByDisplayOrderAscClassIdAsc(
            Integer branchId, Long academicYearId);

    /** Public admission class definitions are scoped to the branch, not an academic year. */
    @Query("select c from SchoolClass c where c.branch.branchId = :branchId and c.level.levelId = :levelId and c.status = :status order by c.displayOrder asc, c.classId asc")
    List<SchoolClass> findPublicClassesByBranchAndLevel(@Param("branchId") Integer branchId,
                                                        @Param("levelId") Integer levelId,
                                                        @Param("status") Integer status);

    /** Public admission class definitions are reusable across academic years. */
    @Query("select c from SchoolClass c where c.branch.branchId = :branchId and c.status = :status order by c.displayOrder asc, c.classId asc")
    List<SchoolClass> findPublicClassesByBranch(@Param("branchId") Integer branchId,
                                                @Param("status") Integer status);
    /**
     * Classes available for a selected academic year. A class becomes
     * available in its introduction year and remains available in later years.
     * The selected year and class introduction year must belong to the same branch.
     */
    @Query("""
            select c
            from SchoolClass c
            join c.academicYear introductionYear
            join ErpAcademicYear selectedYear
              on selectedYear.academicYearId = :academicYearId
            where c.branch.branchId = :branchId
              and introductionYear.branch.branchId = :branchId
              and selectedYear.branch.branchId = :branchId
              and selectedYear.active = true
              and introductionYear.startDate <= selectedYear.startDate
              and c.status = :status
              and (:levelId is null or c.level.levelId = :levelId)
            order by c.displayOrder asc, c.classId asc
            """)
    List<SchoolClass> findAvailableForBranchAndAcademicYear(
            @Param("branchId") Integer branchId,
            @Param("academicYearId") Long academicYearId,
            @Param("levelId") Integer levelId,
            @Param("status") Integer status);

    /** Validates class eligibility for a selected academic year on the server. */
    @Query("""
            select case when count(c) > 0 then true else false end
            from SchoolClass c
            join c.academicYear introductionYear
            join ErpAcademicYear selectedYear
              on selectedYear.academicYearId = :academicYearId
            where c.classId = :classId
              and c.branch.branchId = :branchId
              and introductionYear.branch.branchId = :branchId
              and selectedYear.branch.branchId = :branchId
              and selectedYear.active = true
              and introductionYear.startDate <= selectedYear.startDate
              and c.status = :status
            """)
    boolean existsAvailableForBranchAndAcademicYear(
            @Param("classId") Integer classId,
            @Param("branchId") Integer branchId,
            @Param("academicYearId") Long academicYearId,
            @Param("status") Integer status);

    /** Checks whether a level has active classes available in the selected academic year. */
    @Query("""
            select case when count(c) > 0 then true else false end
            from SchoolClass c
            join c.academicYear introductionYear
            join ErpAcademicYear selectedYear
              on selectedYear.academicYearId = :academicYearId
            where c.branch.branchId = :branchId
              and c.level.levelId = :levelId
              and c.level.status = :status
              and introductionYear.branch.branchId = :branchId
              and selectedYear.branch.branchId = :branchId
              and selectedYear.active = true
              and introductionYear.startDate <= selectedYear.startDate
              and c.status = :status
            """)
    boolean existsAvailableForBranchAndAcademicYearAndLevel(
            @Param("branchId") Integer branchId,
            @Param("academicYearId") Long academicYearId,
            @Param("levelId") Integer levelId,
            @Param("status") Integer status);

}
