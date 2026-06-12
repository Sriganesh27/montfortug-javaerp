package com.erp.montfortuganda.auth.dto;

import lombok.Data;

@Data
public class AuthRequest {
    private String username;
    private String password;
    private Integer branchId; // NEW
    private String role;      // NEW
}