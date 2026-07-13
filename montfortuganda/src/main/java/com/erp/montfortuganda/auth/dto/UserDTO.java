package com.erp.montfortuganda.auth.dto;

import com.erp.montfortuganda.school.dto.BranchDTO;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserDTO {
    private Integer id;

    @NotBlank(message = "Username is required")
    private String username;

    private String password; // Only used for creation/update payloads

    private Long roleId; // Can be used to resolve the role code securely

    private String role; // Legacy string role, optional if roleId is provided

    private Integer assignedBranchId;
    private BranchDTO assignedBranch;

    private Integer isActive;
}