package com.erp.montfortuganda.school.dto;

import com.erp.montfortuganda.school.enums.DepartmentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class DepartmentDTO {

    private Long departmentId;
    private String departmentCode;

    @NotBlank(message = "Department Name is required")
    @Size(max = 100, message = "Department Name cannot exceed 100 characters")
    private String departmentName;

    @NotNull(message = "Department Type is required")
    private DepartmentType departmentType;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    private Boolean active;
    private Integer displayOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private long employeeCount;
    private long designationCount;

    public DepartmentDTO(Long departmentId, String departmentCode, String departmentName,
                         DepartmentType departmentType, String description, Boolean active,
                         Integer displayOrder, LocalDateTime createdAt, LocalDateTime updatedAt,
                         long designationCount, long employeeCount) {
        this.departmentId = departmentId;
        this.departmentCode = departmentCode;
        this.departmentName = departmentName;
        this.departmentType = departmentType;
        this.description = description;
        this.active = active;
        this.displayOrder = displayOrder;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.designationCount = designationCount;
        this.employeeCount = employeeCount;
    }
}