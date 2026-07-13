// File: src/main/java/com/erp/montfortuganda/employee/repository/EmployeeQualificationRepository.java
package com.erp.montfortuganda.employee.repository;

import com.erp.montfortuganda.employee.entity.ErpEmployeeQualification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EmployeeQualificationRepository extends JpaRepository<ErpEmployeeQualification, Long> {
    List<ErpEmployeeQualification> findByEmployee_EmployeeIdAndEmployeeQualificationActiveTrue(Long employeeId);
}