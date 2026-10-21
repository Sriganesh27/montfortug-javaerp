package com.erp.montfortuganda.auth.dto;

import com.erp.montfortuganda.auth.enums.LoginStatus;

import java.time.LocalDateTime;

/** Safe, read-only login-history view without internal session markers or credentials. */
public record LoginHistoryResponse(
        LocalDateTime loginTime,
        LocalDateTime logoutTime,
        String deviceName,
        String browser,
        String ipAddress,
        LoginStatus status
) { }
