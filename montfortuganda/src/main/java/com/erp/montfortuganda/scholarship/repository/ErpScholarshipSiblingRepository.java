package com.erp.montfortuganda.scholarship.repository;

import com.erp.montfortuganda.scholarship.entity.ErpScholarshipSibling;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ErpScholarshipSiblingRepository
        extends JpaRepository<ErpScholarshipSibling, Long> {

    @Query("""
            select s
            from ErpScholarshipSibling s
            where s.scholarshipApplication.scholarshipAppId = :scholarshipAppId
              and s.active = true
            order by s.scholarshipSiblingId asc
            """)
    List<ErpScholarshipSibling>
    findByScholarshipApplicationScholarshipAppIdAndActiveTrueOrderBySiblingIdAsc(
            @Param("scholarshipAppId") Long scholarshipAppId
    );

    @Query("""
            select s
            from ErpScholarshipSibling s
            where s.scholarshipApplication.scholarshipAppId = :scholarshipAppId
            order by s.scholarshipSiblingId asc
            """)
    List<ErpScholarshipSibling>
    findByScholarshipApplicationScholarshipAppIdOrderBySiblingIdAsc(
            @Param("scholarshipAppId") Long scholarshipAppId
    );

    void deleteByScholarshipApplicationScholarshipAppId(
            Long scholarshipAppId
    );
}
