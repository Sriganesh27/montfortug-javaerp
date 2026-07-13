package com.erp.montfortuganda.employee.dto;

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
    private LocalDate startDate;
    private LocalDate endDate;

    // For file uploads
    private String fileData;
    private String fileName;
}