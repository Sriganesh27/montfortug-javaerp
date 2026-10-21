package com.erp.montfortuganda.auth.controller;

import com.erp.montfortuganda.auth.dto.ActiveDeviceResponse;
import com.erp.montfortuganda.auth.dto.LoginHistoryResponse;
import com.erp.montfortuganda.auth.service.CurrentUserContext;
import com.erp.montfortuganda.auth.service.CurrentUserService;
import com.erp.montfortuganda.auth.service.SessionManagementService;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Read-only account-security endpoints scoped to the currently authenticated user. */
@RestController
@RequestMapping("/api/auth/security")
public class AccountSecurityController {

    private final CurrentUserService currentUserService;
    private final SessionManagementService sessionManagementService;

    public AccountSecurityController(
            CurrentUserService currentUserService,
            SessionManagementService sessionManagementService
    ) {
        this.currentUserService = currentUserService;
        this.sessionManagementService = sessionManagementService;
    }

    @GetMapping("/sessions")
    public ResponseEntity<List<ActiveDeviceResponse>> getActiveDevices() {
        CurrentUserContext user = currentUserService.getCurrentUserContext();
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(sessionManagementService.getActiveDevices(user.getUserId()));
    }

    @GetMapping("/login-history")
    public ResponseEntity<List<LoginHistoryResponse>> getLoginHistory() {
        CurrentUserContext user = currentUserService.getCurrentUserContext();
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(sessionManagementService.getLoginHistory(user.getUserId()));
    }
}
