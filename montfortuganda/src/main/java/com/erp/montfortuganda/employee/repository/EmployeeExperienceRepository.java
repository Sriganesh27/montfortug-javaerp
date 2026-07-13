// File: src/main/java/com/erp/montfortuganda/employee/repository/EmployeeExperienceRepository.java
package com.erp.montfortuganda.employee.repository;

import com.erp.montfortuganda.employee.entity.ErpEmployeeExperience;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EmployeeExperienceRepository extends JpaRepository<ErpEmployeeExperience, Long> {
    List<ErpEmployeeExperience> findByEmployee_EmployeeIdAndEmployeeExperienceActiveTrue(Long employeeId);
}