// File: src/main/java/com/erp/montfortuganda/employee/dto/EmployeeExperienceDTO.java
package com.erp.montfortuganda.employee.dto;

import com.erp.montfortuganda.employee.enums.ExperienceEmploymentType;
import lombok.Data;
import java.time.LocalDate;

@Data
public class EmployeeExperienceDTO {
    private Long employeeExperienceId;
    private Long employeeId;
    private String employeeExperienceCompanyName;
    private String employeeExperienceDesignation;
    private ExperienceEmploymentType employeeExperienceEmploymentType;
    private LocalDate employeeExperienceStartDate;
    private LocalDate employeeExperienceEndDate;
    private Boolean employeeExperienceCurrentJob;
    private Boolean employeeExperienceVerified;
    private Boolean employeeExperienceActive;
}