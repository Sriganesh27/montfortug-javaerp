package com.erp.montfortuganda.school.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class DesignationDTO {

    private Long designationId;
    private String designationCode;

    @NotBlank(message = "Designation Name is required")
    @Size(max = 100, message = "Designation Name cannot exceed 100 characters")
    private String designationName;

    @NotNull(message = "Department is required")
    private Long departmentId;

    private String departmentName;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    private Boolean active;

    @Min(value = 1, message = "Display Order must be at least 1")
    @Max(value = 999, message = "Display Order cannot exceed 999")
    private Integer displayOrder;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private long employeeCount;

    // Compile-time safe constructor used by Repository Projections
    public DesignationDTO(Long designationId, String designationCode, String designationName,
                          Long departmentId, String departmentName, String description,
                          Boolean active, Integer displayOrder, LocalDateTime createdAt,
                          LocalDateTime updatedAt, long employeeCount) {
        this.designationId = designationId;
        this.designationCode = designationCode;
        this.designationName = designationName;
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.description = description;
        this.active = active;
        this.displayOrder = displayOrder;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.employeeCount = employeeCount;
    }
}