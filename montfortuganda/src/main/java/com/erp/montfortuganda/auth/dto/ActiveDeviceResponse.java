package com.erp.montfortuganda.auth.dto;

import java.time.LocalDateTime;

/** Safe, read-only view of a user's active session; never exposes the session token. */
public record ActiveDeviceResponse(
        String deviceName,
        String browser,
        String operatingSystem,
        String ipAddress,
        LocalDateTime loginTime,
        LocalDateTime lastActivityTime,
        LocalDateTime expiryTime
) { }
