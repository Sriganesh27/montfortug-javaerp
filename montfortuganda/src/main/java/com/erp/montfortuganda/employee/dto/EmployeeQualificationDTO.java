// File: src/main/java/com/erp/montfortuganda/employee/dto/EmployeeQualificationDTO.java
package com.erp.montfortuganda.employee.dto;

import com.erp.montfortuganda.employee.enums.QualificationLevel;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class EmployeeQualificationDTO {
    private Long employeeQualificationId;
    private Long employeeId;
    private QualificationLevel employeeQualificationLevel;
    private String employeeQualificationName;
    private String employeeQualificationInstitutionName;
    private Integer employeeQualificationCompletionYear;
    private String employeeQualificationGrade;
    private BigDecimal employeeQualificationPercentage;
    private Boolean employeeQualificationVerified;
    private Boolean employeeQualificationActive;
}