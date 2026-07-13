// File: src/main/java/com/erp/montfortuganda/employee/dto/EmployeeResponse.java
package com.erp.montfortuganda.employee.dto;

import com.erp.montfortuganda.employee.enums.EmployeeCategory;
import com.erp.montfortuganda.employee.enums.EmployeeType;
import com.erp.montfortuganda.employee.enums.EmploymentMode;
import com.erp.montfortuganda.employee.enums.EmploymentStatus;
import lombok.Data;
import java.time.LocalDate;

@Data
public class EmployeeResponse {
    private Long employeeId;
    private String employeeNo;
    private String fullName;
    private String email;
    private String phone;
    private Long departmentId;
    private Long designationId;
    private Long reportingManagerId;
    private EmployeeCategory category;
    private EmployeeType employeeType;
    private EmploymentMode employmentMode;
    private EmploymentStatus status;
    private LocalDate joiningDate;
}