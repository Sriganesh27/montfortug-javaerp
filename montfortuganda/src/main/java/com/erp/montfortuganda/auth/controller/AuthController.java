package com.erp.montfortuganda.auth.controller;

import com.erp.montfortuganda.auth.dto.AuthRequest;
import com.erp.montfortuganda.auth.dto.AuthResponse;
import com.erp.montfortuganda.auth.dto.ChangeTemporaryPasswordRequest;
import com.erp.montfortuganda.auth.entity.CredentialDeliveryStatus;
import com.erp.montfortuganda.auth.entity.User;
import com.erp.montfortuganda.auth.jwt.JwtUtil;
import com.erp.montfortuganda.auth.repository.UserRepository;
import com.erp.montfortuganda.auth.service.UserCredentialService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(
                    AuthController.class
            );

    private static final String ADMIN_GATEWAY =
            "SECURE_ADMIN_GATEWAY";

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final UserCredentialService userCredentialService;

    public AuthController(
            AuthenticationManager authenticationManager,
            JwtUtil jwtUtil,
            UserRepository userRepository,
            UserCredentialService userCredentialService
    ) {
        this.authenticationManager =
                authenticationManager;

        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.userCredentialService =
                userCredentialService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody AuthRequest request
    ) {
        try {
            Authentication authentication =
                    authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(
                                    request.getUsername(),
                                    request.getPassword()
                            )
                    );

            if (
                    !(authentication.getPrincipal()
                            instanceof UserDetails userDetails)
            ) {
                LOGGER.error(
                        "Authenticated principal is not UserDetails."
                );

                return invalidCredentialsResponse();
            }

            User dbUser =
                    userRepository
                            .findByUsernameWithAssignedBranch(
                                    userDetails.getUsername()
                            )
                            .orElseThrow(
                                    () -> new IllegalStateException(
                                            "Authenticated user was not found."
                                    )
                            );

            if (
                    isAdministrativeUser(dbUser)
                            && !ADMIN_GATEWAY.equals(
                            request.getRole()
                    )
            ) {
                LOGGER.warn(
                        "Administrative login blocked outside the secure gateway for username: {}",
                        dbUser.getUsername()
                );

                return invalidCredentialsResponse();
            }

            Integer branchId =
                    dbUser.getAssignedBranch() == null
                            ? null
                            : dbUser
                            .getAssignedBranch()
                            .getBranchId();

            if (
                    !Integer.valueOf(1)
                            .equals(dbUser.getIsActive())
            ) {
                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body(
                                new AuthResponse(
                                        null,
                                        dbUser.getRole(),
                                        branchId,
                                        AuthResponse.Status.ACCOUNT_DISABLED,
                                        "This account is inactive. Contact the administrator.",
                                        null,
                                        null
                                )
                        );
            }

            if (
                    Boolean.TRUE.equals(
                            dbUser.getMustChangePassword()
                    )
            ) {
                return handleTemporaryPasswordLogin(
                        dbUser,
                        userDetails,
                        branchId
                );
            }

            return createAuthenticatedResponse(
                    dbUser,
                    userDetails,
                    branchId
            );

        } catch (AuthenticationException exception) {
            LOGGER.warn(
                    "Login failed for username: {}",
                    safeUsername(request)
            );

            return invalidCredentialsResponse();

        } catch (Exception exception) {
            LOGGER.error(
                    "Unexpected login failure for username: {}",
                    safeUsername(request),
                    exception
            );

            return ResponseEntity
                    .status(
                            HttpStatus.INTERNAL_SERVER_ERROR
                    )
                    .body(
                            Map.of(
                                    "message",
                                    "Login could not be completed."
                            )
                    );
        }
    }

    @PostMapping("/change-temporary-password")
    public ResponseEntity<Map<String, String>>
    changeTemporaryPassword(
            @RequestHeader(HttpHeaders.AUTHORIZATION)
            String authorizationHeader,
            @Valid
            @RequestBody
            ChangeTemporaryPasswordRequest request
    ) {
        try {
            userCredentialService
                    .changeTemporaryPassword(
                            authorizationHeader,
                            request
                    );

            return ResponseEntity.ok(
                    Map.of(
                            "status",
                            "PASSWORD_CHANGED",
                            "message",
                            "Password changed successfully. Log in again using your new password."
                    )
            );

        } catch (SecurityException exception) {
            LOGGER.warn(
                    "Temporary-password change rejected: {}",
                    exception.getMessage()
            );

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(
                            Map.of(
                                    "status",
                                    "INVALID_PASSWORD_CHANGE_TOKEN",
                                    "message",
                                    exception.getMessage()
                            )
                    );

        } catch (IllegalArgumentException exception) {
            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "status",
                                    "PASSWORD_CHANGE_REJECTED",
                                    "message",
                                    exception.getMessage()
                            )
                    );

        } catch (IllegalStateException exception) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(
                            Map.of(
                                    "status",
                                    "PASSWORD_CHANGE_NOT_ALLOWED",
                                    "message",
                                    exception.getMessage()
                            )
                    );

        } catch (Exception exception) {
            LOGGER.error(
                    "Unexpected temporary-password change failure.",
                    exception
            );

            return ResponseEntity
                    .status(
                            HttpStatus.INTERNAL_SERVER_ERROR
                    )
                    .body(
                            Map.of(
                                    "status",
                                    "PASSWORD_CHANGE_FAILED",
                                    "message",
                                    "Password could not be changed."
                            )
                    );
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        ResponseCookie cookie =
                expiredJwtCookie();

        return ResponseEntity
                .ok()
                .header(
                        HttpHeaders.SET_COOKIE,
                        cookie.toString()
                )
                .body(
                        Map.of(
                                "message",
                                "Logged out successfully"
                        )
                );
    }

    private ResponseEntity<AuthResponse>
    handleTemporaryPasswordLogin(
            User dbUser,
            UserDetails userDetails,
            Integer branchId
    ) {
        LocalDateTime expiresAt =
                dbUser.getTemporaryPasswordExpiresAt();

        LocalDateTime now =
                LocalDateTime.now(ZoneOffset.UTC);

        if (
                expiresAt == null
                        || !expiresAt.isAfter(now)
        ) {
            if (
                    dbUser.getCredentialDeliveryStatus()
                            != CredentialDeliveryStatus.EXPIRED
            ) {
                dbUser.setCredentialDeliveryStatus(
                        CredentialDeliveryStatus.EXPIRED
                );

                userRepository.save(dbUser);
            }

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(
                            new AuthResponse(
                                    null,
                                    dbUser.getRole(),
                                    branchId,
                                    AuthResponse.Status
                                            .TEMPORARY_PASSWORD_EXPIRED,
                                    "The temporary password has expired. Ask the administrator to resend credentials.",
                                    null,
                                    expiresAt
                            )
                    );
        }

        String passwordChangeToken =
                jwtUtil.generatePasswordChangeToken(
                        userDetails,
                        dbUser.getCredentialVersion()
                );

        return ResponseEntity.ok(
                new AuthResponse(
                        null,
                        dbUser.getRole(),
                        branchId,
                        AuthResponse.Status
                                .PASSWORD_CHANGE_REQUIRED,
                        "Change your temporary password before continuing.",
                        passwordChangeToken,
                        expiresAt
                )
        );
    }

    private ResponseEntity<AuthResponse>
    createAuthenticatedResponse(
            User dbUser,
            UserDetails userDetails,
            Integer branchId
    ) {
        String token =
                jwtUtil.generateToken(userDetails);

        ResponseCookie jwtCookie =
                ResponseCookie
                        .from("jwt_token", token)
                        .httpOnly(true)
                        .secure(true)
                        .path("/")
                        .maxAge(
                                jwtUtil
                                        .getJwtExpirationInMs()
                                        / 1000
                        )
                        .sameSite("Strict")
                        .build();

        return ResponseEntity
                .ok()
                .header(
                        HttpHeaders.SET_COOKIE,
                        jwtCookie.toString()
                )
                .body(
                        new AuthResponse(
                                null,
                                dbUser.getRole(),
                                branchId,
                                AuthResponse.Status.AUTHENTICATED,
                                "Login successful.",
                                null,
                                null
                        )
                );
    }

    private ResponseEntity<AuthResponse>
    invalidCredentialsResponse() {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(
                        new AuthResponse(
                                null,
                                null,
                                null,
                                AuthResponse.Status
                                        .INVALID_CREDENTIALS,
                                "Invalid username or password.",
                                null,
                                null
                        )
                );
    }

    private ResponseCookie expiredJwtCookie() {
        return ResponseCookie
                .from("jwt_token", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();
    }

    private boolean isAdministrativeUser(
            User user
    ) {
        String role =
                user.getRole() == null
                        ? ""
                        : user.getRole()
                        .trim()
                        .toUpperCase(Locale.ROOT);

        return role.equals("SUPER_ADMIN")
                || role.equals("ROLE_SUPER_ADMIN")
                || role.equals("BRANCH_ADMIN")
                || role.equals("ROLE_BRANCH_ADMIN");
    }

    private String safeUsername(
            AuthRequest request
    ) {
        if (
                request == null
                        || request.getUsername() == null
                        || request.getUsername().isBlank()
        ) {
            return "[EMPTY]";
        }

        return request.getUsername().trim();
    }
}