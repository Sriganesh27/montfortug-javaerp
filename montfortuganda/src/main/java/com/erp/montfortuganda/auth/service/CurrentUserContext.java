package com.erp.montfortuganda.auth.service;

import lombok.Data;

import java.util.List;

@Data
public class CurrentUserContext {

    private Integer userId;
    private String username;
    private List<String> roles;

    private Integer branchId;
    private String branchName;
    private String schoolCode;
}