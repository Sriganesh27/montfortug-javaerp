// File: src/main/java/com/erp/montfortuganda/employee/dto/EmployeeSearchCriteria.java
package com.erp.montfortuganda.employee.dto;

import com.erp.montfortuganda.employee.enums.EmployeeCategory;
import com.erp.montfortuganda.employee.enums.EmploymentStatus;
import lombok.Data;

@Data
public class EmployeeSearchCriteria {
    private String keyword; // Searches name, email, employeeNo
    private Long departmentId;
    private Long designationId;
    private EmployeeCategory category;
    private EmploymentStatus status;
}