package com.erp.montfortuganda.infrastructure.sequence.student.repository;

import com.erp.montfortuganda.infrastructure.sequence.student.entity.ErpStudentSequence;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface StudentSequenceRepository
        extends JpaRepository<ErpStudentSequence, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select sequence
            from ErpStudentSequence sequence
            where sequence.branchId = :branchId
              and sequence.moduleCode = :moduleCode
              and sequence.runningYear = :runningYear
            """)
    Optional<ErpStudentSequence> findForUpdate(
            @Param("branchId") Long branchId,
            @Param("moduleCode") String moduleCode,
            @Param("runningYear") Integer runningYear
    );
}
