// File: src/main/java/com/erp/montfortuganda/employee/dto/EmployeeAccountRequest.java
package com.erp.montfortuganda.employee.dto;

import com.erp.montfortuganda.employee.enums.LoginMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.List;

@Data
public class EmployeeAccountRequest {

    // --- PROVISIONING TRIGGERS ---
    private Boolean generateLogin = false;
    private Boolean autoGeneratePassword = true;

    // --- CREDENTIALS ---
    @Size(max = 100)
    private String username;

    @NotNull(message = "Login method is required")
    private LoginMethod loginMethod = LoginMethod.USERNAME;

    private Boolean forcePasswordReset = true;
    private Boolean accountEnabled = true;

    // --- ROLE ASSIGNMENT ---
    private Long roleId;

    private List<Long> additionalPermissionIds;

    // --- NOTIFICATIONS & AUDIT ---
    private Boolean sendEmail = false;
    private Boolean sendSMS = false;

    @Size(max = 1000)
    private String accountRemarks;
}