// File: src/main/java/com/erp/montfortuganda/employee/repository/EmployeeDocumentRepository.java
package com.erp.montfortuganda.employee.repository;

import com.erp.montfortuganda.employee.entity.ErpEmployeeDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EmployeeDocumentRepository extends JpaRepository<ErpEmployeeDocument, Long> {
    List<ErpEmployeeDocument> findByEmployee_EmployeeIdAndEmployeeDocumentActiveTrue(Long employeeId);
}