package com.erp.montfortuganda.employee.dto;

import com.erp.montfortuganda.employee.enums.ExperienceEmploymentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeExperienceRequest {

    private Long employeeExperienceId;

    @NotBlank(
            message = "Experience company name is required"
    )
    private String companyName;

    private String jobRole;

    @NotNull(
            message = "Experience employment type is required"
    )
    private ExperienceEmploymentType
            employeeExperienceEmploymentType;

    @NotNull(
            message = "Experience start date is required"
    )
    private LocalDate startDate;

    private LocalDate endDate;

    private String fileData;

    private String fileName;
}