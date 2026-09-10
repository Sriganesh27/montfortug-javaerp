package com.erp.montfortuganda.auth.dto;

public record HeaderContextResponse(
        String username,
        String role,
        String branchName,
        String schoolCode,
        String branchLocation,
        String logoUrl
) {
}