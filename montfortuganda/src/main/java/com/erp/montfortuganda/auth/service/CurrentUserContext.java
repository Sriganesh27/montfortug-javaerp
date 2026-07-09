package com.erp.montfortuganda.auth.service;

import lombok.Data;
import java.util.List;

@Data
public class CurrentUserContext {
    private Integer userId;
    private String username;

    // Supports multiple roles for a single user later
    private List<String> roles;

    private Long schoolId;
    private Integer branchId;
    private String branchName;

    // Added for DocumentNumberService integration
    private String schoolCode;
}