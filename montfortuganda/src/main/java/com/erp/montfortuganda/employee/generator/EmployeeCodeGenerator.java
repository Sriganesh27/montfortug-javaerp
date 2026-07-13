// File: src/main/java/com/erp/montfortuganda/employee/generator/EmployeeCodeGenerator.java
package com.erp.montfortuganda.employee.generator;

import com.erp.montfortuganda.employee.enums.EmployeeCategory;
import java.time.LocalDate;

public interface EmployeeCodeGenerator {
    /**
     * Generates a unique employee code in the format: {SchoolCode}-{Category}-{JoinYear}-{Sequence}
     * Example: U011-T-26-001 or U021-NT-26-001
     */
    String generateCode(Integer branchId, EmployeeCategory category, LocalDate joiningDate);
}