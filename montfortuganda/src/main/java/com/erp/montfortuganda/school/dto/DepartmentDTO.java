package com.erp.montfortuganda.school.dto;

import com.erp.montfortuganda.model.enums.RecordStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DepartmentDTO {
    private Long departmentId;

    @NotNull(message = "Branch ID is required")
    private Integer branchId;

    @NotBlank(message = "Department code is required")
    @Size(max = 20, message = "Code must not exceed 20 characters")
    @Pattern(regexp = "^[a-zA-Z_]+$", message = "Code must contain only letters and underscores")
    private String departmentCode;

    @NotBlank(message = "Department name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String departmentName;

    private Boolean isAcademic = true;
    private String description;

    private RecordStatus status;
    private Boolean active;
    private Long version;
    private LocalDateTime createdAt;
    private String createdBy;
}