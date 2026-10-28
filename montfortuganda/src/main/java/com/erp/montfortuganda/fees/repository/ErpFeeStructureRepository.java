package com.erp.montfortuganda.fees.repository;
import com.erp.montfortuganda.fees.entity.ErpFeeStructure;
import com.erp.montfortuganda.fees.entity.ErpFeeStructure.StructureStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;
public interface ErpFeeStructureRepository extends JpaRepository<ErpFeeStructure,Long> {
 @EntityGraph(attributePaths={"branch","academicYear","level","startClass","endClass"})
 List<ErpFeeStructure> findAllByBranch_BranchId(Integer branchId);

 @EntityGraph(attributePaths={"branch","academicYear","level","startClass","endClass"})
 Optional<ErpFeeStructure> findByFeeStructureIdAndBranch_BranchId(Long id,Integer branchId);
 @EntityGraph(attributePaths={"branch","academicYear","level","startClass","endClass"})
 @Query(value="""
 select s from ErpFeeStructure s where s.branch.branchId=:branchId
 and (:yearId is null or s.academicYear.academicYearId=:yearId)
 and (:levelId is null or s.level.levelId=:levelId)
 and (:status is null or s.structureStatus=:status)
 and (:search is null or lower(s.feeName) like concat('%',:search,'%')
      or lower(s.feeType) like concat('%',:search,'%')
      or lower(coalesce(s.feeCode,'')) like concat('%',:search,'%')
      or lower(coalesce(s.remarks,'')) like concat('%',:search,'%'))
 order by s.academicYear.academicYearId desc, s.level.displayOrder, s.startClass.displayOrder, s.versionNo desc, s.feeStructureId desc
 """,
 countQuery="""
 select count(s) from ErpFeeStructure s where s.branch.branchId=:branchId
 and (:yearId is null or s.academicYear.academicYearId=:yearId)
 and (:levelId is null or s.level.levelId=:levelId)
 and (:status is null or s.structureStatus=:status)
 and (:search is null or lower(s.feeName) like concat('%',:search,'%')
      or lower(s.feeType) like concat('%',:search,'%')
      or lower(coalesce(s.feeCode,'')) like concat('%',:search,'%')
      or lower(coalesce(s.remarks,'')) like concat('%',:search,'%'))
 """)
 Page<ErpFeeStructure> searchForBranch(@Param("branchId") Integer branchId,@Param("yearId") Long yearId,
 @Param("levelId") Integer levelId,@Param("status") StructureStatus status,@Param("search") String search,Pageable pageable);
 @Lock(LockModeType.PESSIMISTIC_WRITE)
 @Query("select s from ErpFeeStructure s where s.feeStructureId=:id and s.branch.branchId=:branchId")
 Optional<ErpFeeStructure> findForUpdate(@Param("id") Long id,@Param("branchId") Integer branchId);
 boolean existsByBranch_BranchIdAndAcademicYear_AcademicYearIdAndLevel_LevelIdAndStartClass_ClassIdAndFeeNameIgnoreCaseAndFeeTypeIgnoreCaseAndActiveTrue(
 Integer branchId,Long yearId,Integer levelId,Integer startClassId,String feeName,String feeType);
 @Query("select coalesce(max(s.versionNo),0) from ErpFeeStructure s where s.branch.branchId=:branchId and s.academicYear.academicYearId=:yearId and s.level.levelId=:levelId and s.startClass.classId=:startClassId and s.feeName=:feeName and s.feeType=:feeType")
 Integer maxVersion(@Param("branchId") Integer branchId,@Param("yearId") Long yearId,@Param("levelId") Integer levelId,@Param("startClassId") Integer startClassId,@Param("feeName") String feeName,@Param("feeType") String feeType);
}
