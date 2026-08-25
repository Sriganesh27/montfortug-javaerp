package com.erp.montfortuganda.scholarship.repository;

import com.erp.montfortuganda.scholarship.entity.ErpScholarshipSibling;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ErpScholarshipSiblingRepository
        extends JpaRepository<ErpScholarshipSibling, Long> {

    List<ErpScholarshipSibling>
    findByScholarshipApplicationScholarshipAppIdAndActiveTrueOrderBySiblingIdAsc(
            Long scholarshipAppId
    );

    List<ErpScholarshipSibling>
    findByScholarshipApplicationScholarshipAppIdOrderBySiblingIdAsc(
            Long scholarshipAppId
    );

    void deleteByScholarshipApplicationScholarshipAppId(
            Long scholarshipAppId
    );
}
