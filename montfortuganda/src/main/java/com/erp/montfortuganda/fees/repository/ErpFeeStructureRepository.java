package com.erp.montfortuganda.fees.repository;

import com.erp.montfortuganda.fees.entity.ErpFeeStructure;
import com.erp.montfortuganda.fees.entity.ErpFeeStructure.StructureStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ErpFeeStructureRepository extends JpaRepository<ErpFeeStructure, Long> {

    @EntityGraph(attributePaths = {"branch", "academicYear", "schoolClass", "schoolClass.level", "term"})
    Optional<ErpFeeStructure> findByFeeStructureIdAndBranch_BranchId(
            Long feeStructureId, Integer branchId);

    @EntityGraph(attributePaths = {"branch", "academicYear", "schoolClass", "schoolClass.level", "term"})
    @Query("""
            select s from ErpFeeStructure s
            where s.branch.branchId = :branchId
              and (:yearId is null or s.academicYear.academicYearId = :yearId)
              and (:classId is null or s.schoolClass.classId = :classId)
              and (:termId is null or s.term.termId = :termId)
              and (:status is null or s.structureStatus = :status)
            order by s.academicYear.startDate desc, s.term.displayOrder asc,
                     s.schoolClass.displayOrder asc, s.versionNo desc
            """)
    List<ErpFeeStructure> searchForBranch(
            @Param("branchId") Integer branchId,
            @Param("yearId") Long yearId,
            @Param("classId") Integer classId,
            @Param("termId") Long termId,
            @Param("status") StructureStatus status);

    @EntityGraph(attributePaths = {"branch", "academicYear", "schoolClass", "schoolClass.level", "term"})
    List<ErpFeeStructure> findAllByBranch_BranchIdAndSchoolClass_ClassIdAndTerm_TermIdOrderByVersionNoDesc(
            Integer branchId, Integer classId, Long termId);

    boolean existsBySchoolClass_ClassIdAndTerm_TermIdAndVersionNo(
            Integer classId, Long termId, Integer versionNo);

    boolean existsBySchoolClass_ClassIdAndTerm_TermIdAndStructureStatus(
            Integer classId, Long termId, StructureStatus status);

    @Query("""
            select coalesce(max(s.versionNo), 0)
            from ErpFeeStructure s
            where s.schoolClass.classId = :classId
              and s.term.termId = :termId
            """)
    Integer findMaximumVersion(
            @Param("classId") Integer classId,
            @Param("termId") Long termId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select s from ErpFeeStructure s
            where s.feeStructureId = :id
              and s.branch.branchId = :branchId
            """)
    Optional<ErpFeeStructure> findForUpdate(
            @Param("id") Long id,
            @Param("branchId") Integer branchId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select s from ErpFeeStructure s
            where s.schoolClass.classId = :classId
              and s.term.termId = :termId
              and s.structureStatus = :status
            """)
    List<ErpFeeStructure> findByClassTermAndStatusForUpdate(
            @Param("classId") Integer classId,
            @Param("termId") Long termId,
            @Param("status") StructureStatus status);
}
