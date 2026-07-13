// File: src/main/java/com/erp/montfortuganda/employee/repository/EmployeeContactRepository.java
package com.erp.montfortuganda.employee.repository;

import com.erp.montfortuganda.employee.entity.ErpEmployeeContact;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EmployeeContactRepository extends JpaRepository<ErpEmployeeContact, Long> {
    List<ErpEmployeeContact> findByEmployee_EmployeeIdAndEmployeeContactActiveTrue(Long employeeId);
}