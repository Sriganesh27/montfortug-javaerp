package com.erp.montfortuganda.school.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BranchDTO {
    private Integer branchId;

    @NotBlank(message = "Branch Name is required")
    private String branchName;

    @Size(max = 10, message = "School code cannot exceed 10 characters")
    private String schoolCode;

    @NotBlank(message = "Branch Type is required")
    private String branchType;

    private String branchLocation;
    private Integer isActive;
}