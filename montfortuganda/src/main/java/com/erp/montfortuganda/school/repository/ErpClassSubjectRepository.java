package com.erp.montfortuganda.school.repository;

import com.erp.montfortuganda.school.entity.ErpClassSubject;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ErpClassSubjectRepository extends JpaRepository<ErpClassSubject, Long> {

    @EntityGraph(attributePaths = {"academicYear", "level", "subject", "startClass", "endClass"})
    List<ErpClassSubject> findAllByBranch_BranchIdAndAcademicYear_AcademicYearIdAndActiveTrueOrderByLevel_DisplayOrderAscStartClass_DisplayOrderAscSubject_DisplayOrderAsc(
            Integer branchId, Long academicYearId);

    @EntityGraph(attributePaths = {"academicYear", "level", "subject", "startClass", "endClass"})
    Optional<ErpClassSubject> findByClassSubjectIdAndBranch_BranchId(
            Long classSubjectId, Integer branchId);

    List<ErpClassSubject> findAllByBranch_BranchIdAndAcademicYear_AcademicYearIdAndActiveTrue(
            Integer branchId, Long academicYearId);
}
