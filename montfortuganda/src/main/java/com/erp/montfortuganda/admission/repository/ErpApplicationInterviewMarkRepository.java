package com.erp.montfortuganda.admission.repository;

import com.erp.montfortuganda.admission.entity.ErpApplicationInterviewMark;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ErpApplicationInterviewMarkRepository
        extends JpaRepository<ErpApplicationInterviewMark, Long> {

    List<ErpApplicationInterviewMark>
    findAllByInterview_InterviewIdAndActiveTrueOrderByInterviewMarkIdAsc(
            Long interviewId
    );

    Optional<ErpApplicationInterviewMark>
    findByInterview_InterviewIdAndSubjectIdAndActiveTrue(
            Long interviewId,
            Long subjectId
    );

    boolean existsByInterview_InterviewIdAndSubjectIdAndActiveTrue(
            Long interviewId,
            Long subjectId
    );

    long countByInterview_InterviewIdAndActiveFalse(
            Long interviewId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select m
            from ErpApplicationInterviewMark m
            join m.interview i
            join i.application a
            where m.interviewMarkId = :interviewMarkId
              and a.branch.branchId = :branchId
              and m.active = true
              and i.active = true
              and a.status = 1
            """)
    Optional<ErpApplicationInterviewMark>
    findActiveByIdAndBranchForUpdate(
            @Param("interviewMarkId") Long interviewMarkId,
            @Param("branchId") Integer branchId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select m
            from ErpApplicationInterviewMark m
            join m.interview i
            join i.application a
            where i.interviewId = :interviewId
              and a.branch.branchId = :branchId
              and m.active = true
              and i.active = true
              and a.status = 1
            order by m.interviewMarkId asc
            """)
    List<ErpApplicationInterviewMark>
    findAllActiveByInterviewAndBranchForUpdate(
            @Param("interviewId") Long interviewId,
            @Param("branchId") Integer branchId
    );

    @Modifying
    @Query("""
            update ErpApplicationInterviewMark m
               set m.active = false,
                   m.updatedBy = :updatedBy,
                   m.updatedAt = CURRENT_TIMESTAMP
             where m.interview.interviewId = :interviewId
               and m.active = true
            """)
    int deactivateAllByInterviewId(
            @Param("interviewId") Long interviewId,
            @Param("updatedBy") Long updatedBy
    );
}
