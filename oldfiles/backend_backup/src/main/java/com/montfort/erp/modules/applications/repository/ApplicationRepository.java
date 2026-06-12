package com.montfort.erp.modules.applications.repository;

import com.montfort.erp.modules.applications.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    long countByBranchIdAndAcademicYear(Long branchId, String academicYear);
}

