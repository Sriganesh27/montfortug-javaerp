package com.erp.montfortuganda.school.repository;

import com.erp.montfortuganda.school.entity.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BranchRepository
        extends JpaRepository<Branch, Integer> {

    boolean existsBySchoolCodeIgnoreCase(
            String schoolCode
    );

    boolean existsBySchoolCodeIgnoreCaseAndBranchIdNot(
            String schoolCode,
            Integer branchId
    );

    boolean existsByBranchEmailIgnoreCase(
            String branchEmail
    );

    boolean existsByBranchEmailIgnoreCaseAndBranchIdNot(
            String branchEmail,
            Integer branchId
    );

    Optional<Branch> findBySchoolCodeIgnoreCase(
            String schoolCode
    );

    @Query("""
            SELECT DISTINCT branch
            FROM Branch branch
            LEFT JOIN FETCH branch.branchLevels branchLevel
            LEFT JOIN FETCH branchLevel.level
            ORDER BY branch.branchId DESC
            """)
    List<Branch> findAllWithLevels();

    @Query("""
            SELECT DISTINCT branch
            FROM Branch branch
            LEFT JOIN FETCH branch.branchLevels branchLevel
            LEFT JOIN FETCH branchLevel.level
            WHERE branch.branchId = :branchId
            """)
    Optional<Branch> findByIdWithLevels(
            @Param("branchId") Integer branchId
    );
}