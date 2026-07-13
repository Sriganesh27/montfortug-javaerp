// File: src/main/java/com/erp/montfortuganda/employee/dto/response/EmployeeListResponse.java
package com.erp.montfortuganda.employee.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeListResponse {
    private Long employeeId;
    private String employeeNo;
    private String fullName;
    private String departmentName;
    private String designationName;
    private String employeeCategory;
    private String employmentStatus;
    private String officialEmail;
    private String mobileNo;
    private Boolean active;
}