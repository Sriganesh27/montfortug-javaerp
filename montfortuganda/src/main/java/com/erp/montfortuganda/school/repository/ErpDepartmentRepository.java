package com.erp.montfortuganda.school.repository;

import com.erp.montfortuganda.school.ErpDepartment;
import com.erp.montfortuganda.school.enums.DepartmentType;
import com.erp.montfortuganda.school.projection.DepartmentSearchProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface ErpDepartmentRepository extends JpaRepository<ErpDepartment, Long> {

    Optional<ErpDepartment> findByDepartmentIdAndActiveTrue(Long departmentId);

    @Query("SELECT d.departmentId AS departmentId, d.departmentCode AS departmentCode, " +
            "d.departmentName AS departmentName, d.departmentType AS departmentType, " +
            "d.description AS description, d.active AS active, d.displayOrder AS displayOrder, " +
            "d.createdAt AS createdAt, d.updatedAt AS updatedAt, " +
            "0L AS designationCount, " +
            "(SELECT COUNT(emp) FROM ErpEmployee emp WHERE emp.department.departmentId = d.departmentId AND emp.active = true) AS employeeCount " +
            "FROM ErpDepartment d WHERE " +
            "(:branchId IS NULL OR d.branch.branchId = :branchId) " +
            "AND (:active IS NULL OR d.active = :active) " +
            "AND (:keyword IS NULL OR LOWER(d.departmentName) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:departmentCode IS NULL OR d.departmentCode = :departmentCode) " +
            "AND (:type IS NULL OR d.departmentType = :type) " +
            "AND (:createdAfter IS NULL OR d.createdAt >= :createdAfter) " +
            "AND (:createdBefore IS NULL OR d.createdAt <= :createdBefore)")
    Page<DepartmentSearchProjection> searchDepartments(
            @Param("branchId") Integer branchId,
            @Param("keyword") String keyword,
            @Param("departmentCode") String departmentCode,
            @Param("type") DepartmentType type,
            @Param("active") Boolean active,
            @Param("createdAfter") LocalDateTime createdAfter,
            @Param("createdBefore") LocalDateTime createdBefore,
            Pageable pageable);

    boolean existsByDepartmentNameIgnoreCaseAndBranch_BranchIdAndActiveTrue(String departmentName, Integer branchId);
    Optional<ErpDepartment> findByDepartmentNameIgnoreCaseAndBranch_BranchIdAndActiveTrue(String departmentName, Integer branchId);
}

