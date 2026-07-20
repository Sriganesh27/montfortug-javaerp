package com.erp.montfortuganda.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangeTemporaryPasswordRequest {

    @NotBlank(message = "Current temporary password is required.")
    private String currentPassword;

    @NotBlank(message = "New password is required.")
    @Size(
            min = 8,
            max = 100,
            message = "New password must contain between 8 and 100 characters."
    )
    private String newPassword;

    @NotBlank(message = "Password confirmation is required.")
    private String confirmPassword;
}