package com.erp.montfortuganda.employee.dto;

import com.erp.montfortuganda.employee.enums.ExperienceEmploymentType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeExperienceRequest {

    private Long employeeExperienceId;
    private String companyName;
    private String jobRole;

    private ExperienceEmploymentType employeeExperienceEmploymentType;

    private LocalDate startDate;
    private LocalDate endDate;

    private String fileData;
    private String fileName;
}