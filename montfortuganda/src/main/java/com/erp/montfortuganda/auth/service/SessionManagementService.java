package com.erp.montfortuganda.auth.service;

import com.erp.montfortuganda.auth.entity.ErpLoginHistory;
import com.erp.montfortuganda.auth.entity.ErpUserSession;
import com.erp.montfortuganda.auth.dto.ActiveDeviceResponse;
import com.erp.montfortuganda.auth.dto.LoginHistoryResponse;
import com.erp.montfortuganda.auth.entity.User;
import com.erp.montfortuganda.auth.enums.LoginStatus;
import com.erp.montfortuganda.auth.repository.ErpLoginHistoryRepository;
import com.erp.montfortuganda.auth.repository.ErpUserSessionRepository;
import com.erp.montfortuganda.school.entity.Branch;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class SessionManagementService {

    private static final String SESSION_MARKER_PREFIX = "[session:";

    private final ErpUserSessionRepository sessionRepository;
    private final ErpLoginHistoryRepository loginHistoryRepository;

    public SessionManagementService(
            ErpUserSessionRepository sessionRepository,
            ErpLoginHistoryRepository loginHistoryRepository
    ) {
        this.sessionRepository = sessionRepository;
        this.loginHistoryRepository = loginHistoryRepository;
    }

    /** Registers a successful login and links its history record to the opaque session key. */
    @Transactional
    public void registerSuccessfulLogin(
            User user,
            String sessionKey,
            LocalDateTime expiryTime,
            HttpServletRequest request
    ) {
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("A persisted user is required to register a session.");
        }
        if (sessionKey == null || sessionKey.isBlank()) {
            throw new IllegalArgumentException("A session key is required.");
        }
        if (expiryTime == null) {
            throw new IllegalArgumentException("A session expiry time is required.");
        }

        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        RequestDeviceInfo deviceInfo = resolveDeviceInfo(request);

        ErpUserSession session = new ErpUserSession();
        session.setUser(user);
        session.setSessionToken(sessionKey);
        session.setLoginTime(now);
        session.setLastActivityTime(now);
        session.setExpiryTime(expiryTime);
        session.setIpAddress(deviceInfo.ipAddress());
        session.setDeviceName(deviceInfo.deviceName());
        session.setBrowser(deviceInfo.browser());
        session.setOperatingSystem(deviceInfo.operatingSystem());
        session.setActive(true);
        session.setCreatedBy(user.getId().longValue());
        session.setUpdatedBy(user.getId().longValue());
        sessionRepository.save(session);

        ErpLoginHistory history = new ErpLoginHistory();
        history.setUser(user);
        Branch branch = user.getAssignedBranch();
        if (branch != null) {
            history.setBranch(branch);
        }
        history.setLoginTime(now);
        history.setIpAddress(deviceInfo.ipAddress());
        history.setDeviceName(deviceInfo.deviceName());
        history.setBrowserName(deviceInfo.browser());
        history.setLoginStatus(LoginStatus.SUCCESS);
        // The opaque UUID is only a correlation marker, not an authentication credential.
        // The security UI should display the human-readable portion only.
        history.setRemarks("Successful login " + sessionMarker(sessionKey));
        history.setActive(true);
        loginHistoryRepository.save(history);
    }

    /** Revokes one session identified by the session ID inside the signed JWT. */
    @Transactional
    public boolean revokeSession(String sessionKey, HttpServletRequest request) {
        if (sessionKey == null || sessionKey.isBlank()) {
            return false;
        }

        Optional<ErpUserSession> match = sessionRepository.findBySessionToken(sessionKey);
        if (match.isEmpty()) {
            return false;
        }

        ErpUserSession session = match.get();
        if (!Boolean.TRUE.equals(session.getActive())) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        session.setActive(false);
        session.setLastActivityTime(now);
        if (session.getUser() != null && session.getUser().getId() != null) {
            session.setUpdatedBy(session.getUser().getId().longValue());
        }
        sessionRepository.save(session);
        closeLoginHistory(session.getUser().getId(), sessionKey, now, "User logged out");
        return true;
    }

    /** Revokes all other active sessions for the authenticated user, preserving this session. */
    @Transactional
    public int revokeOtherSessions(
            Integer authenticatedUserId,
            String currentSessionKey,
            HttpServletRequest request
    ) {
        if (authenticatedUserId == null) {
            throw new IllegalArgumentException("An authenticated user is required.");
        }
        if (currentSessionKey == null || currentSessionKey.isBlank()) {
            throw new IllegalArgumentException("The current session could not be identified. Please sign in again.");
        }

        List<ErpUserSession> otherSessions =
                sessionRepository.findByUser_IdAndActiveTrueAndSessionTokenNot(
                        authenticatedUserId,
                        currentSessionKey
                );

        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        int revokedCount = 0;
        for (ErpUserSession session : otherSessions) {
            if (session.getSessionToken() == null
                    || session.getSessionToken().equals(currentSessionKey)) {
                continue;
            }

            session.setActive(false);
            session.setLastActivityTime(now);
            session.setUpdatedBy(authenticatedUserId.longValue());
            sessionRepository.save(session);
            closeLoginHistory(authenticatedUserId, session.getSessionToken(), now,
                    "Session revoked from another device");
            revokedCount++;
        }
        return revokedCount;
    }

    /** Returns active, non-expired sessions for the authenticated user only. */
    @Transactional(readOnly = true)
    public List<ActiveDeviceResponse> getActiveDevices(Integer userId) {
        if (userId == null) {
            throw new IllegalArgumentException("An authenticated user is required.");
        }

        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        return sessionRepository
                .findByUser_IdAndActiveTrueAndExpiryTimeAfterOrderByLastActivityTimeDesc(userId, now)
                .stream()
                .map(session -> new ActiveDeviceResponse(
                        safeDisplay(session.getDeviceName(), "Unknown device"),
                        safeDisplay(session.getBrowser(), "Unknown browser"),
                        safeDisplay(session.getOperatingSystem(), "Unknown OS"),
                        safeDisplay(session.getIpAddress(), "Unknown"),
                        session.getLoginTime(),
                        session.getLastActivityTime(),
                        session.getExpiryTime()
                ))
                .toList();
    }

    /** Returns the latest 50 login-history records for the authenticated user only. */
    @Transactional(readOnly = true)
    public List<LoginHistoryResponse> getLoginHistory(Integer userId) {
        if (userId == null) {
            throw new IllegalArgumentException("An authenticated user is required.");
        }

        return loginHistoryRepository.findTop50ByUser_IdOrderByLoginTimeDesc(userId)
                .stream()
                .map(history -> new LoginHistoryResponse(
                        history.getLoginTime(),
                        history.getLogoutTime(),
                        safeDisplay(history.getDeviceName(), "Unknown device"),
                        safeDisplay(history.getBrowserName(), "Unknown browser"),
                        safeDisplay(history.getIpAddress(), "Unknown"),
                        history.getLoginStatus()
                ))
                .toList();
    }

    private String safeDisplay(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private void closeLoginHistory(
            Integer userId,
            String sessionKey,
            LocalDateTime logoutTime,
            String logoutRemarks
    ) {
        if (userId == null || sessionKey == null) {
            return;
        }
        String marker = sessionMarker(sessionKey);
        Optional<ErpLoginHistory> matchingHistory = loginHistoryRepository
                .findFirstByUser_IdAndRemarksContainingOrderByLoginTimeDesc(userId, marker);

        // Compatibility fallback for sessions created by Stage 1/2 before the
        // per-session marker was written to login-history remarks.
        if (matchingHistory.isEmpty()) {
            ErpUserSession session = sessionRepository.findBySessionToken(sessionKey).orElse(null);
            if (session != null && session.getLoginTime() != null) {
                matchingHistory = loginHistoryRepository
                        .findFirstByUser_IdAndLoginTimeAndLogoutTimeIsNullOrderByLoginHistoryIdDesc(
                                userId, session.getLoginTime());
            }
        }

        matchingHistory.ifPresent(history -> {
            if (history.getLogoutTime() == null) {
                history.setLogoutTime(logoutTime);
                history.setLoginStatus(LoginStatus.LOGOUT);
                history.setRemarks(logoutRemarks);
                loginHistoryRepository.save(history);
            }
        });
    }

    private String sessionMarker(String sessionKey) {
        return SESSION_MARKER_PREFIX + sessionKey + "]";
    }

    private RequestDeviceInfo resolveDeviceInfo(HttpServletRequest request) {
        if (request == null) {
            return new RequestDeviceInfo(null, "Unknown device", "Unknown browser", "Unknown OS");
        }

        String userAgent = request.getHeader("User-Agent");
        String browser = detectBrowser(userAgent);
        String operatingSystem = detectOperatingSystem(userAgent);
        String deviceName = operatingSystem + " device";

        return new RequestDeviceInfo(
                limit(request.getRemoteAddr(), 45),
                limit(deviceName, 255),
                limit(browser, 150),
                limit(operatingSystem, 150)
        );
    }

    private String detectBrowser(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) return "Unknown browser";
        String value = userAgent.toLowerCase(Locale.ROOT);
        if (value.contains("edg/")) return "Microsoft Edge";
        if (value.contains("opr/") || value.contains("opera")) return "Opera";
        if (value.contains("firefox/")) return "Mozilla Firefox";
        if (value.contains("chrome/") && !value.contains("chromium/")) return "Google Chrome";
        if (value.contains("safari/") && value.contains("version/")) return "Safari";
        return "Other browser";
    }

    private String detectOperatingSystem(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) return "Unknown OS";
        String value = userAgent.toLowerCase(Locale.ROOT);
        if (value.contains("windows")) return "Windows";
        if (value.contains("iphone") || value.contains("ipad") || value.contains("ios")) return "iOS";
        if (value.contains("android")) return "Android";
        if (value.contains("mac os") || value.contains("macintosh")) return "macOS";
        if (value.contains("linux")) return "Linux";
        return "Unknown OS";
    }

    private String limit(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) return value;
        return value.substring(0, maxLength);
    }

    private record RequestDeviceInfo(
            String ipAddress,
            String deviceName,
            String browser,
            String operatingSystem
    ) { }
}
