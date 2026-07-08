package com.erp.montfortuganda.auth.service;

import lombok.Data;

@Data
public class CurrentUserContext {
    private Integer userId;
    private String username;
    private String roleCode;

    // Extracted safely so we don't pass the full Branch Entity!
    private Integer branchId;
    private String branchName;
}