package com.erp.montfortuganda.scholarship.repository;

import com.erp.montfortuganda.scholarship.entity.ErpScholarshipHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

public interface ErpScholarshipHistoryRepository
        extends JpaRepository<ErpScholarshipHistory, Long> {

    List<ErpScholarshipHistory>
    findAllByScholarshipApplicationScholarshipAppIdOrderByScholarshipHistoryIdDesc(
            Long scholarshipAppId
    );

    Optional<ErpScholarshipHistory>
    findFirstByScholarshipApplicationScholarshipAppIdOrderByScholarshipHistoryIdDesc(
            Long scholarshipAppId
    );

    /**
     * Finds the history snapshot for one Scholarship application and one
     * requested academic-year/term cycle.
     *
     * A Scholarship application may have multiple history records over time.
     * The requested year and term identify the historical snapshot instead of
     * overwriting a previous year's record.
     */
    Optional<ErpScholarshipHistory>
    findFirstByScholarshipApplicationScholarshipAppIdAndAcademicYearAndTermRequestedOrderByScholarshipHistoryIdDesc(
            Long scholarshipAppId,
            String academicYear,
            String termRequested
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select h
            from ErpScholarshipHistory h
            where h.scholarshipHistoryId = :historyId
            """)
    Optional<ErpScholarshipHistory>
    findByIdForUpdate(
            @Param("historyId") Long historyId
    );

    List<ErpScholarshipHistory>
    findAllByStudentIdAndAcademicYearOrderByScholarshipHistoryIdDesc(
            Long studentId,
            String academicYear
    );

    List<ErpScholarshipHistory>
    findAllByBranchIdAndAcademicYearOrderByScholarshipHistoryIdDesc(
            Long branchId,
            String academicYear
    );
}