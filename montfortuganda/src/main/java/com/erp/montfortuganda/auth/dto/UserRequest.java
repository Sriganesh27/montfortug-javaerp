package com.erp.montfortuganda.auth.dto;

import lombok.Data;

@Data
public class UserRequest {
    private String username;
    private String password;
    private String role; // e.g., "SCHOOL_ADMIN"
    private Integer branchId; // The ID of the school they belong to
}