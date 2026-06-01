package com.montfort.erp.repository;

import com.montfort.erp.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    long countByBranchIdAndAcademicYear(Long branchId, String academicYear);
}
